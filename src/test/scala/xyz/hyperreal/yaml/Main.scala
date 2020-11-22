package xyz.hyperreal.yaml

import xyz.hyperreal.pretty.prettyPrint

object Main extends App {

  val s =
    """|- one
       |- two
       |
       |""".stripMargin
//    "[1, 2, 3]"
  val ast = YamlParser.parseFromString(s)

  println(prettyPrint(ast))

}
