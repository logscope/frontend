package frontend

import Types._
import common.Util._

/**
 * Provides methods for checking the wellformedness of the abstract syntax tree
 * produced by the parser.
 *
 * The following violations are checked:
 * =====================================
 *
 * 100. Monitors must have different names
 * 110. States must have different names
 * 115. State names must differ from event names
 * 120. Formal state parameters must be different
 * 125. All parameters must be used, unless state is used in conditions
 * 130. Modifiers must be different
 * 140. Modifiers must not be conflicting
 * 145. Initial states cannot have parameters
 * 150. A state must have transitions unless it is used in a condition
 * 170. Conditions and actions must be defined states
 * 180. Events must be one of the declared (if declared)
 * 190. A state must be reachable
 * 200. Actions ok and error not on left hand side
 * 205. Actions ok and error cannot have parameters
 * 210. Only one ok or error on right hand side
 * 220. If ok, then no other action
 * 225. Field names in state constraint must be different
 * 230. Field names in state constraint must form subset of states formal parameters
 * 235. Field names in event constraint must form a subset of event formal parameters (if declared)
 * 240. Range names on right hand side must be in scope
 * 250. Creation of a new state must define all formal parameters
 */

class WellFormed {

  /**
   * Keeps track of how many errors have been encountered.
   */

  private var errors: Int = 0

  /**
   * Variables holding the current monitor and the current state being checked.
   * Used e.g. for printing error messages.
   */

  private var currentMonitor: Option[String] = None
  private var currentState: Option[State] = None

  /**
   * The wellformedness errors detected.
   */

  var wfErrors : WfErrors = new WfErrors

  /**
   * Reports an error detected.
   *
   * @param nr the kind of error, see comment above for the class.
   * @param message the error message.
   */

  def error(nr: Int)(message: String) {
    errors += 1
    var stateName : Option[String] = None
    println()
    println(s"*** error $nr:")
    currentMonitor match {
      case None =>
      case Some(name) => println("monitor " + name + ":")
    }
    currentState match {
      case None =>
      case Some(state) =>
        stateName = Some(state.id)
        println("  " + state)
    }
    println("- " + message)
    wfErrors.add(WfError(nr, currentMonitor, stateName, message))
  }

  // --- wellformed ---

  /**
   * The remaining methods perform the welformedness check.
   */

  def wellformed(spec: Specification): Boolean = {
    spec match {
      case Specification(monitors) =>
        noDuplicates(monitors.map(_.id))(100)("monitor is defined more than once")
        monitors foreach wellformed
    }
    errors == 0
  }

  def wellformed(monitor: Monitor) {
    val Monitor(id, events, states) = monitor
    currentMonitor = Some(id)
    noDuplicates(states.map(_.id))(110)("state is defined more than once")
    states foreach wellformedState(monitor)
    currentMonitor = None
  }

  def wellformedState(monitor: Monitor)(state: State) {
    currentState = Some(state)
    val State(modifiers, id, formals, transitions) = state
    if (monitor.usedEventIds.contains(id) || monitor.eventMap.contains(id)) {
      error(115)(s"Event names cannot be used as state names")
    }
    if (!state.isInitial && !monitor.statesReached.contains(id)) {
      error(190)("state is never reached")
    }
    if (state.isInitial && formals.length > 0) {
      error(145)("initial state cannot have parameters")
    }
    if (state.isInitial || !stateUsedInCondition(id, monitor)) {
      mustHaveTransitionsAndUseAllFormals(formals, transitions)
    }
    wellformedModifiers(modifiers)
    noDuplicates(formals)(120)(s"formal parameter occurs multiple times in $formals")
    wellformedTransitions(monitor, formals)(transitions)
    currentState = None
  }

  def wellformedModifiers(modifiers: List[Modifier]) {
    noDuplicates(modifiers)(130)(s"modifier occurs more than once in $modifiers")
    notTogether(ALWAYS, HOT, modifiers)
    notTogether(ALWAYS, STEP, modifiers)
    notTogether(STEP, NEXT, modifiers)
    notTogether(STEP, HOT, modifiers)
  }

  def notTogether(modifier1: Modifier, modifier2: Modifier, modifiers: List[Modifier]) {
    if (modifiers.contains(modifier1) && modifiers.contains(modifier2)) {
      error(140)("incompatible modifiers: " + modifier1 + ", " + modifier2)
    }
  }

  def stateUsedInCondition(id: Identifier, monitor: Monitor): Boolean = {
    monitor.states exists {
      case state =>
        state.transitions exists {
          case transition =>
            transition.conditions exists {
              case Pattern(_, `id`, _) => true
              case _ => false
            }
        }
    }
  }

  def mustHaveTransitionsAndUseAllFormals(formals: List[Identifier], transitions: List[Transition]) {
    if (transitions.length == 0) {
      error(150)("this state has no exiting transitions and is not used in a transition left hand side")
    } else {
      val usedIds = transitions.flatMap(freeIds).toSet
      val unusedIds = formals.toSet &~ usedIds
      if (!unusedIds.isEmpty) {
        error(125)("unused formal parameter(s): " + setToString(unusedIds))
      }
    }
  }

  def freeIds(transition: Transition): Set[Identifier] = {
    val Transition(event, conditions, actions) = transition
    freeIds(event) ++ conditions.flatMap(freeIds) ++ actions.flatMap(freeIds)
  }

  def freeIds(pattern: Pattern): Set[Identifier] = {
    val Pattern(_, _, constraints) = pattern
    constraints.flatMap(freeIds).toSet
  }

  def freeIds(constraint: Constraint): Set[Identifier] = {
    val Constraint(_, range) = constraint
    freeIds(range)
  }

  def freeIds(range: Range): Set[Identifier] = {
    range match {
      case NameRange(id) => Set(id)
      case _ => Set()
    }
  }

  def wellformedTransitions(monitor: Monitor, stateFormals: List[Identifier])(transitions: List[Transition]) {
    for (transition <- transitions) {
      wellformedTransition(monitor, stateFormals)(transition)
    }
  }

  def wellformedTransition(monitor: Monitor, formals: List[Identifier])(transition: Transition) {
    val Transition(event, conditions, actions) = transition
    val oks = actions.count(_.isOk)
    val errors = actions.count(_.isError)
    if (oks + errors > 1) {
      error(210)("more than one occurrence of 'ok'/'error'")
    }
    if (oks == 1 && actions.length > 1) {
      error(220)("'ok' must occur alone on right hand side of transition")
    }

    wellformedLeftPattern(monitor)(event)
    conditions foreach wellformedLeftPattern(monitor)
    val parameterNamesDefined = formals.toSet ++ rangeNamesDefinedInLhs(event :: conditions)
    actions foreach wellformedRightPattern(monitor, parameterNamesDefined)
  }

  def wellformedLeftPattern(monitor: Monitor)(pattern: Pattern) {
    if (pattern.isOk || pattern.isError) {
      error(200)("'ok'/'error' not allowed on left hand side of transition")
    }
    wellformedPattern(monitor)(pattern)
  }

  def rangeNamesDefinedInLhs(patterns: List[Pattern]): Set[Identifier] = {
    patterns.flatMap(rangeNamesUsedInPattern(_)).toSet
  }


  def rangeNamesUsedInPattern(pattern: Pattern): Set[Identifier] = {
    val Pattern(_, _, constraints) = pattern
    constraints.flatMap(constraint => namesUsedInRange(constraint.range)).toSet
  }

  def namesUsedInRange(range: Range): Set[Identifier] = {
    range match {
      case NameRange(id: Identifier) => Set(id)
      case _ => Set()
    }
  }

  def wellformedRightPattern(monitor: Monitor, parameterNamesInScope: Set[Identifier])(pattern: Pattern) {
    val Pattern(_, id, constraints) = pattern
    if (!pattern.isOk && !pattern.isError) {
      if (monitor.stateMap.isDefinedAt(id)) {
        val usedFieldNamesOfPattern = constraints.map(_.id).toSet
        val definedFieldNamesOfState = monitor.stateMap(id).formals.toSet
        if (definedFieldNamesOfState != usedFieldNamesOfPattern) {
          error(250)("actual field name(s) " +
            setToString(usedFieldNamesOfPattern) +
            " in " + pattern +
            " does/do not match with state " + id + "'s formal parameters " + setToString(definedFieldNamesOfState)
          )
        }
      }
      val undefinedRangeNames = rangeNamesUsedInPattern(pattern).diff(parameterNamesInScope)
      if (!undefinedRangeNames.isEmpty) {
        error(240)("reference to undefined range name " + setToString(undefinedRangeNames) + " in: " + pattern)
      }
      wellformedPattern(monitor)(pattern)
    }
  }

  def wellformedPattern(monitor: Monitor)(pattern: Pattern) {
    val Pattern(_, id, constraints) = pattern
    if (pattern.isOk || pattern.isError) {
      if (!constraints.isEmpty) {
        error(205)(s"ok and error cannot have parameters, ${constraints.mkString(",")}")
      }
    } else {
      noDuplicates(constraints.map(_.id))(225)("actual field name occurs multiple times: " + pattern)
      if (pattern.usedAsState) {
        if (!monitor.stateMap.keySet.contains(pattern.id)) {
          error(170)("state " + pattern.id + " not defined")
        } else {
          val usedFieldNamesOfPattern = constraints.map(_.id).toSet
          val definedFieldNamesOfState = monitor.stateMap(id).formals.toSet
          val undefinedUsedFieldNames = usedFieldNamesOfPattern.diff(definedFieldNamesOfState)
          if (!undefinedUsedFieldNames.isEmpty) {
            error(230)("actual field name(s) " +
              setToString(undefinedUsedFieldNames) +
              " in " + pattern +
              " does/do not match with state " + id + "'s formal parameters."
            )
          }
        }
      } else { // pattern is used as event
        if (!monitor.eventMap.isEmpty) {
          if (!monitor.eventMap.keySet.contains(pattern.id)) {
            error(180)("event " + pattern.id + " not amongst the declared")
          } else {
            val usedFieldNamesOfEvent = constraints.map(_.id).toSet
            val definedFieldNamesOfEvent = monitor.eventMap(id).formals.toSet
            val undefinedUsedFieldNames = usedFieldNamesOfEvent.diff(definedFieldNamesOfEvent)
            if (!undefinedUsedFieldNames.isEmpty) {
              error(235)("actual field name(s) " +
                setToString(undefinedUsedFieldNames) +
                " in event " + pattern +
                " does/do not match with event " + id + "'s formal parameters."
              )
            }
          }
        }
      }
    }
  }

  def noDuplicates[T](items: List[T])(errorNr: Int)(errorMsg: String) {
    items match {
      case Nil =>
      case item :: items_ =>
        if (items_ contains item) {
          error(errorNr)(s"$errorMsg: $item")
        }
        noDuplicates(items_)(errorNr)(errorMsg)
    }
  }
}

/**
 * An instance of this class stores the errors detected.
 */

class WfErrors {
  /**
   * The errors detected.
   */

  private var wfErrors : List[WfError] = Nil

  /**
   * Adds an error detected to the list of such.
   *
   * @param error the error detected.
   */

  def add(error: WfError): Unit = {
    wfErrors ++= List(error)
  }

  /**
   * Returns the number of errors detected (stored).
   *
   * @return the number of errors detected.
   */

  def size: Int = wfErrors.size

  /**
   * Returns the list of errors detected.
   *
   * @return the list of errors detected.
   */

  def getErrors: List[WfError] = wfErrors

  override def toString: String = wfErrors.mkString("\n")

  /**
   * Generates a test oracle from the list of errors detected.
   * Used for creating unit tests. The test oracle is printed on
   * standard out, from where it is copied and pasted into the
   * unit test.
   */

  def generateOracle(): Unit = {
    var result : List[(Int,String)] = Nil
    for (WfError(nr, monitor, state, msg) <- wfErrors) {
      result ++= List((nr, s""""${monitor.getOrElse("")}""""))
    }
    println(result.mkString("    ", ",\n    ", ""))
  }

  /**
   * Tests that the newly detected errors match those provided as argument. Used for unit testing.
   * The argument to the method is generated by the method `generateOracle`.
   *
   * @param contract the errors that are expected, generated by `generateOracle`: error number and
   *                 monitor name for each.
   */

  def testOracle(contract: (Int,String)*): Unit = {
    assert(wfErrors.size == contract.size, s"not the same number of errors. Saw ${wfErrors.size}, expected ${contract.size}")
    val pairs = wfErrors.zip(contract)
    for ((WfError(nr1,optMonitorName1,_,_), (nr2,monitorName2)) <- pairs) {
      val monitorName1 = optMonitorName1 match {
        case None => ""
        case Some(name) => name
      }
      assert(nr1 == nr2, s"saw error number $nr1, expected $nr2")
      assert(monitorName1 == monitorName2, s"saw monitor [$monitorName1], expected [$monitorName2]")
    }
  }
}

/**
 * Represents a single wellformedness error.
 *
 * @param errorNr the kind of error.
 * @param monitor the name of the monitor.
 * @param state the name of the state.
 * @param msg the error message.
 */

case class WfError(errorNr: Int, monitor: Option[String], state: Option[String], msg: String) {
  override def toString = {
    var result = s"$errorNr:"
    monitor match {
      case None =>
      case Some(monitorName) =>
        result += s" monitor $monitorName"
    }
    state match {
      case None =>
      case Some(stateName) =>
        result += s", state $stateName"
    }
    result += s" - $msg"
    result
  }
}