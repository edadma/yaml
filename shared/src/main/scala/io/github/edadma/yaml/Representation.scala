package io.github.edadma.yaml

//import java.time.format.DateTimeFormatter
//import java.time.{LocalDate, LocalTime, ZonedDateTime}

import scala.collection.mutable

object Representation {
  private val FLOAT_REGEX =
    """([-+]?(?:\d+)?\.\d+(?:[Ee][-+]?\d+)?|[-+]?\d+\.\d+[Ee][-+]?\d+|[-+]?\.inf|\.NaN)""" r
  private val DEC_REGEX = """([-+]?(?:0|[123456789]\d*))""" r
  private val HEX_REGEX = """(0x(?:\d|[abcdefABCDEF])+)""" r
  private val OCT_REGEX = """(0o[01234567]+)""" r
  private val DATE_REGEX = """(\d+-\d\d-\d\d)""" r
  private val TIMESTAMP_REGEX =
    """(\d+-\d\d-\d\d[Tt]\d\d:\d\d:\d\d(?:\.\d*)?(?:Z|[+-]\d\d:\d\d))""" r
  private val TIME_REGEX = """([012]\d:[012345]\d:[012345]\d(?:\.\d+)?)""" r
  //  private val SPACED_DATETIME_REGEX =
  //    """(\d+-\d\d-\d\d\s+\d\d:\d\d:\d\d(?:\.\d*)?)""" r
  private val SPACED_TIMESTAMP_REGEX =
    """(\d+-\d\d-\d\d\s+\d\d:\d\d:\d\d(?:\.\d*)?)\s+(Z|[+-]\d(?:\d(?::?\d\d(?::?\d\d)?)?)?)""" r
//  private val SPACED_FORMATTER =
//    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SS]")
}

class Representation {
  import Representation._

  private val anchors = new mutable.HashMap[String, Any]

  private def reset(): Unit = {
    anchors.clear()
  }

  def scalarNode(anchor: Option[String], tag: Option[String], s: String, stringUnlessTag: Boolean): YamlNode = {
    if (anchor isDefined)
      anchors(anchor.get) = s

    if (stringUnlessTag)
      StringYamlNode(s)
    else
      s match {
        case "null"           => NullYamlNode
        case "true" | "false" => BooleanYamlNode(s)
        case DEC_REGEX()      => IntYamlNode(s)
        case _                => StringYamlNode(s)
      }
  }

  def compose(ast: AST): YamlNode =
    ast match {
//      case SourceAST(documents) =>
//        for (d <- documents)
//          yield {
//            reset()
//            compose(d)
//          }
//      case AliasAST(pos, name) =>
//        anchors get name match {
//          case None    => problem(pos, s"anchor not found: $name")
//          case Some(v) => v
//        }
      case EmptyAST                        => NullYamlNode
      case PlainAST(anchor, tag, s)        => scalarNode(anchor, tag, s, stringUnlessTag = false)
      case SingleQuotedAST(anchor, tag, s) => scalarNode(anchor, tag, s, stringUnlessTag = true)
      case DoubleQuotedAST(anchor, tag, s) => scalarNode(anchor, tag, s, stringUnlessTag = true)
      case MapAST(anchor, tag, pairs) =>
        val map = MapYamlNode(pairs map { case (k, v) => (compose(k), compose(v)) })

        if (anchor isDefined)
          anchors(anchor get) = map

        map
      case SeqAST(anchor, tag, elements) =>
        val seq = SeqYamlNode(elements map compose)

        if (anchor isDefined)
          anchors(anchor get) = seq

        seq
    }

}
