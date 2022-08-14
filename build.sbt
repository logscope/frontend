name := "logscope"

version := "0.1"

scalaVersion := "2.13.4"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

// Log4j vulnerability:
// https://www.cisa.gov/uscert/apache-log4j-vulnerability-guidance
// https://www.lightbend.com/blog/log4j2-security-advisory

libraryDependences += "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.17.1"
libraryDependences += "org.apache.logging.log4j" % "log4j-api" % "2.17.1"
libraryDependences += "org.apache.logging.log4j" % "log4j-core" % "2.17.1"

