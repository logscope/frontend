package frontend

/**
 * Various functions for error reporting.
 */

object ErrorReporting {

  /**
   * Counter of number of errors detected.
   */

  var errors : Int  = 0

  /**
   * Reporting an error, and continuing.
   *
   * @param str the error message.
   */

  def error(str : String) {
    errors += 1
    println("\n" + "=" * 7 + " Error " + errors + ": "+ "=" * 7 + "\n" +
      str + "\n" +
      "-" * 22 + "\n")
  }

  /**
   * Reporting an error and terminating.
   *
   * @param str the error message.
   */

  def fatal(str : String) : Unit = {
    println("\n" + "#" * 6 + " Fatal Error:\n\n" + str + "\n\n")
    System.exit(1)
  }

  /**
   * Verifying a condition and terminating with an error message if it is false.
   *
   * @param condition the condition to verify.
   * @param str the error message to be printed if it is false.
   */

  def verify(condition : Boolean) (str : String) {
    if (!condition) {
      fatal(str)
    }
  }

}
