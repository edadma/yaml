package io.github.edadma.yaml

import pprint._

@main def run(): Unit =
  val s =
//    """|a: {}
//       |""".trim.stripMargin
//    """|- - a: asdf
//       |    b: sdfg
//       |  - bnm
//    """.trim.stripMargin
    """|a:
       | b:
       |  - 1
       |  -
       |   c:
       |   d: 2
       |""".stripMargin
//    """|-
//       | - 1
//       | -
//       |  - asdf
//       |- zxcv
//       |""".stripMargin
//    "[asdf, 2, 3]"
  val node = readFromString(s)

  pprintln(node.construct)
