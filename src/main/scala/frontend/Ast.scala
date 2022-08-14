package frontend

import Types._
import common.Printing._
import scala.language.postfixOps

/**
 * This file contains the abstract syntax for the Scope specification language. There is a class
 * for each kind of language construct.
 */

case class Specification(spec: List[Monitor]) {
  override def toString: String = list2String(0, " ", false)(spec)
}

case class Monitor(id: Identifier, events: List[Event], states: List[State]) {
  val stateMap: Map[Identifier, State] = (for (s <- states) yield s.id -> s) toMap
  val eventMap: Map[Identifier, Event] = (for (e <- events) yield e.id -> e) toMap
  var statesReached: Set[String] = Set()
  var usedEventIds: Set[String] = Set()

  override def toString: String = {
    var nl = ""
    var result = "monitor " + id + " {\n"
    for (event <- events) {
      result += s"  event $event\n"
      nl = "\n"
    }
    for (state <- states) {
      result += s"$nl  $state\n"
      nl = "\n"
    }
    result += "}"
    result
  }
}

case class Event(id: Identifier, formals: List[Identifier]) {
  override def toString: String = {
    val argList = if (formals.isEmpty) "" else s"(${formals.mkString(",")})"
    s"$id$argList"
  }
}

case class State(modifiers: List[Modifier], id: Identifier, formals: List[Identifier], transitions: List[Transition]) {
  val isINIT = modifiers contains INIT
  val isALWAYS = modifiers contains ALWAYS
  val isSTEP = modifiers contains STEP
  val isNEXT = modifiers contains NEXT
  val isHOT = modifiers contains HOT

  var isInitial: Boolean = false

  val internal : Boolean = id.startsWith("INTERNAL__")

  override def toString: String = {
    var result = list2String(0, " ", true)(modifiers)
    result += (if (modifiers.length > 0) " " else "")
    if (!internal) result += id
    if (formals.length > 0) result += "(" + list2String(0, ",", true)(formals) + ")"
    if (!internal) result += " "
    if (transitions.length > 0) result += "{\n" + list2String(2, "", false)(transitions) + "\n  }"
    return result
  }
}

abstract class Modifier
object INIT extends Modifier {
  override def toString: String = "init"
}
object ALWAYS extends Modifier {
  override def toString: String = "always"
}
object STEP extends Modifier {
  override def toString: String = "step"
}
object NEXT extends Modifier {
  override def toString: String = "next"
}
object HOT extends Modifier {
  override def toString: String = "hot"
}

case class Transition(event: Pattern, conditions: List[Pattern], actions: List[Pattern]) {
  event.usedAsState = false

  override def toString: String = {
    val conditionString = if (conditions.isEmpty) "" else " @ " + list2String(0, ",", true)(conditions)
    event + conditionString + " => " + list2String(0, ",", true)(actions)
  }
}

case class Pattern(positive: Boolean, id: Identifier, constraints: List[Constraint]) {
  def isOk: Boolean = (id == "ok")
  def isError: Boolean = (id == "error")
  var usedAsState: Boolean = true

  override def toString: String = {
    val arguments =
      if (constraints.isEmpty) "" else
        "(" + list2String(0, ",", true)(constraints) + ")"
    (if (positive) "" else "!") + id + arguments
  }
}

case class Constraint(id: Identifier, range: Range) {
  override def toString: String = id + " : " + range
}

abstract class Range
case class NameRange(id: Identifier) extends Range {
  override def toString: String = id
}
case class ValueRange(v: Value) extends Range {
  override def toString: String = {
    val str = v.toString
    if (v.isInstanceOf[String]) "\"" + str + "\"" else str
  }
}
case object WildcardRange extends Range {
  override def toString: String = "_"
}

