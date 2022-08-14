package translator

import common.PrinterOps
import frontend._

/**
 * Provides methods for translating an abstract syntax tree representing a
 * specification to C++. The result is written to the file `contract.cpp`.
 *
 * @param spec the specification abstract syntax tree to be translated.
 * @param dir the directory into which to write the resulting `contract.cpp` file.
 */

class Translator(spec: Specification, dir : String) extends PrinterOps {
  /**
   * The top level translation method, which will translate the entire specification into
   * C++, and write it to the file `dir/contract.cpp`.
   */

  def translate(): Unit = {
    val Specification(monitors) = spec

    println("\ngenerating C++ code!\n")

    openFile(dir + "/" + "contract.cpp")

    line("/**********************************************/")
    line("/* C++ generated from LogScope specification! */")
    line("/**********************************************/")
    line()
    line("""#include "contract.h"""")
    line()
    line("using namespace std;")
    line()
    for (monitor <- monitors) {
      printMonitor(monitor)
      line()
    }
    line("SpecObject makeContract() {")
    indent {
      line("ast::Spec *spec = new ast::Spec();")
      for (monitor <- monitors) {
        line(s"spec->addMonitor(monitor${monitor.id}());")
      }
      line("SpecObject contract(spec);")
      line("return contract;")
    }
    line("}")

    closeFile()
  }

  def printMonitor(monitor : Monitor): Unit = {
    val Monitor(id, events, states) = monitor
    line(s"ast::Monitor *monitor$id() {")
    indent {
      line(s"""ast::Monitor *$id = new ast::Monitor("$id");""")
      line()
      for (state <- states) {
        printState(state)
      }
      for (event <- events) {
        line(s"$id->addEvent(${quote(event.id)});")
      }
      for (state <- states) {
        line(s"$id->addState(${state.id});")
      }
      line()
      line(s"return $id;")
    }
    line("}")
  }

  def printState(state: State): Unit = {
    var comma = ""
    val State(modifiers, id, formals, transitions) = state
    line(s"""ast::State *$id = new ast::State(""")
    indent {
      // modifier:
      linePrefix("{")
      for (modifier <- modifiers) {
        lineInfix(comma)
        comma = ","
        printModifier(modifier)
      }
      if (!modifiers.contains(INIT) && state.isInitial) {
        lineInfix(comma)
        printModifier(INIT)
      }
      lineSuffix("},")
      // state name:
      line(s"${quote(id)},")
      // formals:
      linePrefix("{")
      comma = ""
      for (formal <- formals) {
        lineInfix(comma)
        comma = ","
        lineInfix(quote(formal))
      }
      lineSuffix("},")
      // transitions:
      if (transitions.isEmpty) {
        line("{}")
      } else {
        line("{")
        indent {
          for ((transition,idx) <- transitions.zipWithIndex) {
            printTransition(transition)
            if (idx < transitions.size - 1) {
              line(",")
            }
          }
        }
        line("}")
      }
    }
    line(");")
    line()
  }

  def printModifier(modifier: Modifier): Unit = {
    lineInfix(s"ast::Modifier::$modifier")
  }

  def printTransition(transition: Transition): Unit = {
    val Transition(event, conditions, actions) = transition
    line("new ast::Transition(")
    indent {
      // event:
      printPattern(event)
      line(",")
      // conditions:
      if (conditions.isEmpty) {
        line("{}")
      } else {
        line("{")
        indent {
          for ((condition,idx) <- conditions.zipWithIndex) {
            printPattern(condition)
            if (idx < conditions.size - 1) {
              line(",")
            }
          }
        }
        line("}")
      }
      line(",")
      // actions:
      line("{")
      indent {
        for ((action,idx) <- actions.zipWithIndex) {
          printPattern(action)
          if (idx < actions.size - 1) {
            line(",")
          }
        }
      }
      line("}")
    }
    line(")")
  }

  def printPattern(pattern: Pattern): Unit = {
    val Pattern(positive, id, constraints) = pattern
    linePrefix("new ast::Pattern(")
    lineInfix(positive.toString)
    lineInfix(",")
    lineInfix(quote(id))
    lineInfix(",")
    lineInfix("{")
    var comma = ""
    for (constraint <- constraints) {
      lineInfix(comma)
      comma = ","
      printConstraint(constraint)
    }
    lineInfix("}")
    lineSuffix(")")
  }

  def printConstraint(constraint: Constraint): Unit = {
    val Constraint(id, range) = constraint
    lineInfix("new ast::Constraint(")
    lineInfix(quote(id))
    lineInfix(",")
    printRange(range)
    lineInfix(")")
  }

  def printRange(range: Range): Unit = {
    lineInfix("new ast::Range(")
    range match {
      case NameRange(id) =>
        lineInfix("ast::Range::Kind::NAME")
        lineInfix(",")
        lineInfix(quote(id))
      case ValueRange(v) =>
        lineInfix("ast::Range::Kind::VALUE")
        lineInfix(",")
        lineInfix(quote(v.toString))
      case WildcardRange =>
        lineInfix("ast::Range::Kind::WILDCARD")
        lineInfix(",")
        lineInfix(quote("_"))
    }
    lineInfix(")")
  }
}
