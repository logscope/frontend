package common

/**
 * Various functions supporting file operations and printing.
 * Used by the translator.
 */

trait PrinterOps {
  import java.io._

  /**
   * The default writing medium is standard out.
   * This variable is changed by `openFile` below to a file instead.
   */

  var pr : PrintStream = System.out

  /**
   * Opens a file for writing. Only one file can be open at a time for writing.
   *
   * @param fileName name of the file to open.
   */

  def openFile(fileName: String): Unit = {
    pr = new PrintStream(new FileOutputStream(fileName))
  }

  /**
   * Close the currently open file.
   */

  def closeFile(): Unit = {
    pr.close()
    pr = System.out
  }

  /**
   * Writing structured text requires indentation. This variable keeps track
   * of the indentation level.
   */

  var indentation: Int = 0

  /**
   * Increases the indentation level.
   */

  def goIn(): Unit = {
    indentation += 1
  }

  /**
   * Decreases the indentation level.
   */

  def goOut(): Unit = {
    indentation -= 1
  }

  /**
   * Writes code indented. That is, increases the indentation level, writes the code,
   * and then decreases the indentation level again.
   *
   * @param code the code to be written indented.
   */

  def indent(code: => Unit): Unit = {
    goIn()
    code
    goOut
  }

  /**
   * Writes the number of indentations determined by the variable `indentation`.
   * Each indentation is two spaces.
   */

  def tab(): Unit = {
    pr.print("  " * indentation)
  }

  /**
   * Writes a space to the current file.
   */

  def space(): Unit = {
    pr.print(" ")
  }

  /**
   * Writes a string to the current file, followed by a newline.
   *
   * @param str the string to be written.
   */

  def line(str: String): Unit = {
    tab()
    pr.println(str)
  }

  /**
   * Writes a newline to the current file.
   */

  def line(): Unit = {
    pr.println()
  }

  /**
   * Writes a newline to the current file.
   */

  def newLine(): Unit = {
    pr.println()
  }

  /**
   * Writes the first part of a line to the current file. It is introduced with indentation.
   *
   * @param str the string to be written.
   */

  def linePrefix(str: String): Unit = {
    tab()
    pr.print(str)
  }

  /**
   * Writes the mid part of a line to the current file. Assumes that indentation has already
   * taken place. No newline is printed, since more text may be written.
   *
   * @param str the string to be written.
   */

  def lineInfix(str: String): Unit = {
    pr.print(str)
  }

  /**
   * Writes the last part of a line to the current file. A newline is written at the end.
   *
   * @param str the string to be written.
   */

  def lineSuffix(str: String): Unit = {
    pr.println(str)
  }

  /**
   * Returns a string with quotes around. This is needed for writing strings as strings with quotes
   * around.
   *
   * @param s the string to be quoted.
   * @return the quoted string.
   */

  def quote(s: String) = s""""$s""""
}
