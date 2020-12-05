package xyz.hyperreal.yaml

import xyz.hyperreal.pretty.prettyPrint

object Main extends App {

  val s =
    """|? [a] : b
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

  println(prettyPrint(ast))

}
