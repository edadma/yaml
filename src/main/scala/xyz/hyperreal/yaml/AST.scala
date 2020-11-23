package xyz.hyperreal.yaml

import xyz.hyperreal.char_reader.CharReader

trait AST

case class SourceAST(documents: List[ValueAST]) extends AST

trait ValueAST extends AST
case object EmptyAST extends ValueAST
case class PlainAST(anchor: Option[String], tag: Option[String], s: String) extends ValueAST
case class SingleQuotedAST(anchor: Option[String], tag: Option[String], s: String) extends ValueAST
case class DoubleQuotedAST(anchor: Option[String], tag: Option[String], s: String) extends ValueAST
case class AliasAST(pos: CharReader, v: String) {
  val anchor: Option[String] = None
}

trait ContainerAST extends ValueAST
case class MapAST(anchor: Option[String], tag: Option[String], pairs: List[(ValueAST, ValueAST)]) extends ContainerAST
case class SeqAST(anchor: Option[String], tag: Option[String], elements: List[ValueAST]) extends ContainerAST
