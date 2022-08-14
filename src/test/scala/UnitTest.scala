
package tests

import common.Options

trait UnitTest {
  var thisTest : Int = 99999

  def setTest(suite : Int) {
  	  this.thisTest = suite
  }

  def pathTo(spec : String) : String = Options.DIR + "/test/scala/tests/test" + thisTest + "/" + spec

  def getSpec(nr : Int = 0) : String = pathTo("spec") + (if (nr == 0) "" else nr.toString)

  val genDir = "src/test/scala/tests/test-output"
  val visDir = "src/test/scala/tests/test-output"
}

