package xyz.hyperreal.yaml

import java.time.{LocalDate, LocalTime, ZonedDateTime}

import scala.collection.mutable

class Representation {

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
