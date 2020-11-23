package xyz.hyperreal.yaml

import xyz.hyperreal.pretty.prettyPrint

object Main extends App {

  val s =
    """|#
       |# extract all new car dealers from www.autobuyselldealers.com
       |#
       |site: 'http://localhost:8080'
       |folder: www.autobuyselldealers.com
       |states:
       |  initial:
       |    entry: |
       |      write( 'initial state' )
       |    goto: main
       |  main:
       |    entry: |
       |      write( 'main state' )
       |    paths:
       |      /listings/dealerships/auto-dealerships-new/*:
       |        #traverse:
       |        code: |
       |          write( path, file )
       |  final:
       |    entry: |
       |      write( 'final state' )
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
