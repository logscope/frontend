package tests

import frontend.SpecReader
import org.junit.Test
import translator.Translator
import visualizer.Visualizer

/**
 * Test program. Runs a selected test.
 */


class TestSome extends tests.UnitTest {
  setTest(7)

  @Test def test1(): Unit = {
    val spec = new SpecReader(getSpec())
    assert(spec.parserError == false)
    assert(spec.wfErrors.getErrors == Nil)

    val translator = new Translator(spec.spec.get, genDir)
    translator.translate()

    val visualizer = new Visualizer(spec.spec.get, visDir)
    visualizer.visualize()
  }
}
