package xyz.hyperreal.yaml

import xyz.hyperreal.pretty.prettyPrint

object Main extends App {

  val s =
    """|a: as#df
    """.trim.stripMargin
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
  val ast = YamlParser.parseFromString(s)

  println(prettyPrint(ast))

}
