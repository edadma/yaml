package io.github.edadma.yaml

import pprint._

object Main extends App {

  val s =
    """|a: 123
       |b: 3.4
       |c: 6.02214076e23
       |""".trim.stripMargin
//    """|- - a: asdf
//       |    b: sdfg
//       |  - bnm
//    """.trim.stripMargin
//    """|a:
//       | b:
//       |  - 1
//       |  -
//       |   c:
//       |   d: 2
//       |""".stripMargin
//    """|-
//       | - 1
//       | -
//       |  - asdf
//       |- zxcv
//       |""".stripMargin
//    "[asdf, 2, 3]"
  val ast = readFromString(s)

  pprintln(ast)

}
