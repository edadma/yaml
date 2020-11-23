package xyz.hyperreal.yaml

import xyz.hyperreal.pretty.prettyPrint

object Main extends App {

  val s =
    """|#
       |# extract all new car dealers from www.autobuyselldealers.com
       |#
       |start: 'http://localhost:8080/'
       |folder: www.autobuyselldealers.com
       |init: |
       |  write( 'initial state' )
       |paths:
       |  - path: /listings/dealerships/auto-dealerships-new/*
       |    key: dealer-page
       |    from: [start, dealer-page]
       |    crawl:
       |    process: |
       |      write( path, file )
       |final: |
       |  write( 'final state' )
    """.trim.stripMargin
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
