package visualizer

import java.io.File

import scala.language.postfixOps
import sys.process._
import common.PrinterOps
import frontend._

/**
 * Offers methods for visualizing the monitors in a specification.
 * For each monitor, a `.dot` file in GraphViz format (https://graphviz.org)
 * is generated, and converted into a `.png` file.
 *
 * @param spec the specification abstract syntax to be visualized.
 * @param dir the directory into which the resulting `.png` files are stored.
 */

class Visualizer(spec: Specification, dir : String) extends PrinterOps {
  /**
   * Variables for keeping track on how many nodes of different kinds (init, ok, error and and-nodes)
   * have been generated. Used for creating unique instances of these nodes (they are numbered).
   * E.g. every error node is unique to its context and is not shared with other contexts.
   */

  var initCount = 0
  var okCount = 0
  var errorCount = 0
  var andCount = 0

  /**
   * Methods for creating unique instances of init, ok, error and and-nodes.
   */

  def newInitState(): String = {
    initCount += 1
    s"init_$initCount"
  }

  def newOkState(): String = {
    okCount += 1
    s"ok_$okCount"
  }

  def newErrorState(): String = {
    errorCount += 1
    s"error_$errorCount"
  }

  def newAndNode(): String = {
    andCount += 1
    s"And_$andCount"
  }

  /**
   * Turns an action pattern into either new ok or new error node, calling the methods above,
   * or simply returns the name of the pattern, which then represents a user-defined state name.
   *
   * @param action
   * @return the resulting state name.
   */

  def targetStateOf(action: Pattern): String = {
    if (action.isOk) newOkState() else if (action.isError) newErrorState() else
      action.id
  }

  /**
   * The main visualization method visualizing the monitors in a specification.
   */

  def visualize(): Unit = {
    val Specification(monitors) = spec

    println("\ngenerating dot files!\n")

    for (monitor <- monitors) {
      printMonitor(monitor)
    }
  }

  def printMonitor(monitor: Monitor): Unit = {
    val Monitor(id, events, states) = monitor
    openFile(s"$dir/$id.dot")
    line()
    line(s"digraph $id {")
    line()
    indent {
      line(s"""graph [label="$id", labelloc=t, fontsize=20]""")
      line()
      for (state <- states) {
        printState(state)
        line()
      }
      for (i <- 1 to initCount) {
        line(s"init_$i[shape=point]")
      }
      for (i <- 1 to okCount) {
        line(s"ok_$i[shape=point,color=green]")
      }
      for (i <- 1 to errorCount) {
        line(s"""error_$i[label="",shape=proteasesite,color=red]""")
      }
      for (i <- 1 to andCount) {
        line(s"""And_$i[label="",shape=triangle]""")
      }
    }
    line("}")
    closeFile()
    // Converting .dot files to .png files
    // Using this odd syntax for invoking UNIX shell commands:
    // https://alvinalexander.com/scala/how-to-redirect-stdout-stdin-external-commands-in-scala/
    (s"dot -Tpng $dir/$id.dot" #> new File(s"$dir/$id.png")) !;
    s"rm  $dir/$id.dot" !
  }

  def printState(state: State): Unit = {
    val State(modifiers, id, formals, transitions) = state
    linePrefix(s"$id[")
    if (state.internal) {
      lineInfix("""label=""""")
    } else {
      lineInfix(s"""label="$id""")
      if (formals.isEmpty) {
        lineInfix("\"")
      } else {
        lineInfix(s"""(${formals.mkString(",")})""")
        lineInfix("\"")
      }
    }
    if (modifiers.contains(HOT)) {
      lineInfix(",shape=invhouse, color=orange")
    } else {
      lineInfix(",shape = rect,color=green")
    }
    if (modifiers.contains(NEXT)) {
      lineInfix(",style = filled")
    } else if (modifiers.contains(STEP)) {
      lineInfix(",style = dashed")
    }
    lineSuffix("]")
    if (modifiers.contains(ALWAYS)) {
      line(s"$id -> $id")
    }
    if (state.isInitial) {
      val initState = newInitState()
      line(s"$initState -> $id")
    }
    for (transition <- transitions) {
      printTransition(id, transition)
    }
  }

  def printTransition(sourceState: String, transition: Transition): Unit = {
    val Transition(event, conditions, actions) = transition
    if (actions.size == 1) {
      val action :: Nil = actions
      val target = targetStateOf(action)
      linePrefix(s"$sourceState -> $target")
      lineInfix("[label=\"")
      printCondition(event)
      for (pattern <- conditions) {
        lineInfix("\\n")
        printCondition(pattern)
      }
      printActionBindings(action)
      lineSuffix("\"]")
    } else {
      val andNode = newAndNode()
      linePrefix(s"$sourceState -> $andNode")
      lineInfix("[label=\"")
      printCondition(event)
      for (pattern <- conditions) {
        lineInfix("\\n")
        printCondition(pattern)
      }
      lineSuffix("\"]")
      for (action <- actions) {
        val target = targetStateOf(action)
        linePrefix(s"$andNode -> $target")
        lineInfix("[label=\"")
        printActionBindings(action)
        lineSuffix("\",style=dotted]")
      }
    }
  }

  def printCondition(pattern: Pattern): Unit = {
    val patternBackSlashed = pattern.toString.replaceAll("\"","\\\\\"")
    lineInfix(patternBackSlashed)
  }

  def printActionBindings(pattern: Pattern): Unit = {
    for (constraint <- pattern.constraints) {
      printActionBinding(constraint)
    }
  }

  def printActionBinding(constraint: Constraint): Unit = {
    val Constraint(id, range) = constraint
    val rangeBackSlashed = range.toString.replaceAll("\"","\\\\\"")
    lineInfix("\\n")
    lineInfix(s"$id := $rangeBackSlashed")
  }

}
