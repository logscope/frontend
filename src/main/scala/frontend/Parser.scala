package frontend

import scala.util.parsing.combinator._
import java.io.FileReader

import common.Util

import scala.language.postfixOps

/**
 * The grammar for the Scope specification language. The grammar is written using
 * Scala's parser combinators, see build.sbt:
 *
 * libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
 *
 * There is a method for each non-terminal.
 */

class Grammar extends JavaTokenParsers {

  // --- internal name generator: ---

  var anonymousStateCounter = 0 // used for automatic generation of state names

  def nextInternalName(): String = {
    anonymousStateCounter += 1
    "INTERNAL__" + anonymousStateCounter
  }

  // --- keywords, symbols, and identifiers: ---

  val name: Parser[String] = "[A-Z_a-z][A-Z_a-z0-9]*".r

  def kw(s: String): Parser[String] = s"$s\\b".r

  val KW_MONITOR = kw("monitor")
  val KW_EVENT = kw("event")
  val KW_INIT = kw("init")
  val KW_ALWAYS = kw("always")
  val KW_STEP = kw("step")
  val KW_NEXT = kw("next")
  val KW_HOT = kw("hot")

  val LPAR = "("
  val RPAR = ")"
  val LCUR = "{"
  val RCUR = "}"
  val ARROW = "=>"
  val COMMA = ","
  val COND = "@"
  val NOT = "!"
  val COLON = ":"
  val UNDERSCORE = "_"

  val reserved: Parser[String] = KW_MONITOR | KW_EVENT | KW_INIT | KW_ALWAYS | KW_STEP | KW_NEXT | KW_HOT

  val identifier: Parser[String] = not(reserved) ~> name

  // --- grammar: ---

  protected override val whiteSpace = """(\s|/\*(.|\n|\r)*?\*/|//.*\n|\#.*\n)+""".r

  def specification: Parser[Specification] =
    rep(monitor) ^^
      (monitors => Specification(monitors))

  def monitor: Parser[Monitor] =
    KW_MONITOR ~> identifier ~! (LCUR ~> rep(eventdef) ~ rep(state) <~ RCUR) ^^ {
      case name ~ (events ~ states) =>
        anonymousStateCounter = 0
        Monitor(name, events.flatten, states)
    }

  def eventdef: Parser[List[Event]] =
    KW_EVENT ~>! rep1sep(event, COMMA) ^^ {
      case events => events
    }

  def event: Parser[Event] =
    identifier ~ opt(LPAR ~> rep1sep(identifier, COMMA) <~ RPAR) ^^ {
      case id ~ opArgList =>
        val args = opArgList.getOrElse(Nil)
        Event(id, args)
    }

  def state: Parser[State] =
    rep1(modifier) ~ (LCUR ~>! rep1(transition) <~ RCUR) ^^ { case modifiers ~ transitions =>
      State(modifiers, nextInternalName(), Nil, transitions)
    } |
      rep(modifier) ~ identifier ~! opt(LPAR ~> repsep(identifier, COMMA) <~ RPAR) ~! opt(LCUR ~> rep1(transition) <~ RCUR) ^^ { case modifiers ~ name ~ optFormals ~ optTransitions =>
        State(modifiers, name, optFormals.getOrElse(Nil), optTransitions.getOrElse(Nil))
      }

  def modifier: Parser[Modifier] =
    KW_INIT ^^ (_ => INIT) |
      KW_ALWAYS ^^ (_ => ALWAYS) |
      KW_STEP ^^ (_ => STEP) |
      KW_NEXT ^^ (_ => NEXT) |
      KW_HOT ^^ (_ => HOT)

  def transition: Parser[Transition] =
    pattern ~! opt(COND ~> repsep(pattern, COMMA)) ~! (ARROW ~> repsep(pattern, COMMA)) ^^ { case eventPat ~ optConditions ~ actions =>
      Transition(eventPat, optConditions.getOrElse(Nil), actions)
    }

  def pattern: Parser[Pattern] =
    opt(NOT) ~ identifier ~! opt(LPAR ~> repsep(constraint, COMMA) <~ RPAR) ^^ { case optNot ~ name ~ optConstraints =>
      val positive = optNot match {
        case None => true
        case Some(_) => false
      }
      val constraintList = optConstraints match {
        case None => Nil
        case Some(constraints) => constraints
      }
      Pattern(positive, name, constraintList)
    }

  def constraint: Parser[Constraint] =
    (identifier ~! COLON ~! range) ^^ { case name ~ _ ~ range => Constraint(name, range) }

  def range: Parser[Range] =
    value ^^ (value => ValueRange(value)) |
      identifier ^^ (name => NameRange(name)) |
      UNDERSCORE ^^ (_ => WildcardRange)

  def value: Parser[Any] =
    stringLiteral ^^ (s => s replace("\"", "")) | // TODO: maybe no need to remove quotes
      wholeNumber ^^ (s => s toInt)

}

/**
 * The parser itself, extending the `Grammar` class.
 */

object Parser extends Grammar {
  /**
   * Parses a file containing a Scope specification, and returns an optional abstract syntax tree
   * where the top node is a `Specification` object.
   *
   * @param file the file to be parsed containing the Scope specification.
   * @return the optional AST node representing the specification. In case of a syntax error,
   *         `None` is returned.
   */

  def parse(file: String): Option[Specification] = {
    Util.headline("Parsing : " + file)
    val reader = new FileReader(file)
    parseAll(specification, reader) match {
      case Success(spec, _) =>
        Some(spec)
      case fail@NoSuccess(_, _) =>
        println(fail)
        None
    }
  }
}

/**
 * Class offering methods for reading a collection of specification files,
 * parsing them and checking the resulting merged abstract syntax tree for
 * well-formedness, also referred to as type checking.
 *
 * Instantiating this class to an object causes the files to be read, parsed,
 * and type checked. The resulting specification of type `Option[Specification]`
 * is stored in the variable `spec`. It will hold the value `None` if there are
 * syntax errors or type checking errors.
 *
 * @param files the specification files to be parsed.
 */

case class SpecReader(files: List[String]) {
  def this(file: String) = this(List(file))

  Util.bannerLogScope()
  println("Version 1.0.0")

  /**
   *  Variables to keep track of syntax respectively type checking errors encountered.
   */

  var parserError: Boolean = false
  var wfErrors: WfErrors = new WfErrors

  /**
   * The specification abstract syntax object resulting from parsing.
   */

  val spec: Option[Specification] = readSpecification(files)

  /**
   * Method for reading a collection of specification files, parsing them, and
   * applying type checking.
   *
   * @param files the specification files to be parsed.
   * @return the optional specification abstract syntax tree. `None` is returned
   *         if there are syntax errors or type checking errors.
   */

  private def readSpecification(files: List[String]): Option[Specification] = {
    var monitors: List[frontend.Monitor] = Nil
    for (file <- files) {
      Parser.parse(file) match {
        case None =>
          parserError = true
        case Some(Specification(parsedMonitors)) =>
          monitors ++= parsedMonitors
      }
    }
    if (parserError) {
      None
    } else {
      val finalSpec = Specification(monitors)
      println("\nspecification parses!\n")
      println(finalSpec)
      println("\nnow checking for well-formedness ...\n")
      updateSpecification(finalSpec)
      val wellFormedChecker = new WellFormed
      if (wellFormedChecker.wellformed(finalSpec)) {
        println("\nspecification is wellformed!\n")
        Some(finalSpec)
      } else {
        wfErrors = wellFormedChecker.wfErrors
        println(s"\nspecification is not wellformed and contains ${wfErrors.size} errors:\n")
        println(wfErrors)
        None
      }
    }
  }

  /**
   * Updates some variables in the specification abstract syntax tree, useful for performing
   * type checking and translation.
   *
   * @param spec the specification abstract syntax tree to be updated.
   */

  private def updateSpecification(spec: Specification) {
    spec match {
      case Specification(monitors) =>
        for (monitor@Monitor(id, events, states) <- monitors) {
          val anyINIT = states exists (_.isINIT)
          val anyINTERNAL = states exists (_.internal)
          var firstState = true
          for (state <- states) {
            state.isInitial = state.isINIT || state.internal || (!anyINIT && !anyINTERNAL && firstState)
            firstState = false
            for (Transition(event, _, actions) <- state.transitions) {
              monitor.usedEventIds += event.id
              monitor.statesReached ++= (for (a <- actions if !a.isError && !a.isOk) yield a.id).toSet
            }
          }
        }
    }
  }
}

