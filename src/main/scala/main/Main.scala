package main

import frontend.SpecReader
import translator.Translator
import visualizer.Visualizer

/**
 * Main program translating a collection of specifications into monitors in C++.
 * Generates a C++ file `contract.cpp` and a folder with dot files, one for each
 * monitor.
 */

// How to generate a jar file (an artifact):
// https://www.youtube.com/watch?v=HTcvhziJamM

object Main {
  def main(args: Array[String]) = {
    val files = args.toList
    println()
    println("parsing file(s):")
    println()
    for (file <- files) {
      println("  " + file)
    }

    val spec = new SpecReader(files)
    if (spec.parserError == false && spec.wfErrors.getErrors == Nil) {
      val (genDir, vizDir) = createOutputDirs()

      val translator = new Translator(spec.spec.get, genDir)
      translator.translate()

      val visualizer = new Visualizer(spec.spec.get, vizDir)
      visualizer.visualize()
    }
  }

  def createOutputDirs(): (String, String) = {
    import scala.language.postfixOps
    import sys.process._

    val G = "logscope-generated"
    val V = G + "/" + "monitors-visualized"

    s"rm -rf $G" !;
    s"mkdir $G" !;
    s"mkdir $V" !;

    (G, V)
  }
}
