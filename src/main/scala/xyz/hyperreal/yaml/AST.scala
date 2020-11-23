package xyz.hyperreal.yaml

import java.time.{LocalDate, LocalTime, ZonedDateTime}

import xyz.hyperreal.char_reader.CharReader

trait AST

case class SourceAST(documents: List[ValueAST]) extends AST

trait ValueAST extends AST {
  val anchor: Option[String]
}

trait PrimitiveAST extends ValueAST {
  val v: Any
}

case class BooleanAST(anchor: Option[String], v: Boolean) extends PrimitiveAST
case class StringAST(anchor: Option[String], v: String) extends PrimitiveAST
case class NumberAST(anchor: Option[String], v: Number) extends PrimitiveAST
//case class NullAST(anchor: Option[String]) extends PrimitiveAST { val v: Null = null }
case object NullAST extends PrimitiveAST { val anchor: Option[String] = None; val v: Null = null }
case class DateAST(anchor: Option[String], v: LocalDate) extends PrimitiveAST
case class TimestampAST(anchor: Option[String], v: ZonedDateTime) extends PrimitiveAST
case class TimeAST(anchor: Option[String], v: LocalTime) extends PrimitiveAST
case class AliasAST(pos: CharReader, v: String) extends PrimitiveAST {
  val anchor: Option[String] = None
}

trait ContainerAST extends ValueAST
case class MapAST(anchor: Option[String], pairs: List[(ValueAST, ValueAST)]) extends ContainerAST
case class ListAST(anchor: Option[String], elements: List[ValueAST]) extends ContainerAST
