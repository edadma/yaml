package xyz.hyperreal.yaml

abstract class YamlNode extends Dynamic {
  val tag: YamlTag

  def construct: tag.Native = tag.construct(asInstanceOf[tag.Rep])

  //

  def isSeq: Boolean = isInstanceOf[SeqYamlNode]

  def isMap: Boolean = isInstanceOf[MapYamlNode]

  //

  def seq: Seq[YamlNode] = asInstanceOf[SeqYamlNode].elems

  def map: Map[String, YamlNode] = asInstanceOf[MapYamlNode].entries map { case (k, v) => k.string -> v } toMap

  def string: String = asInstanceOf[StringYamlNode].scalar

  def boolean: Boolean = asInstanceOf[BooleanYamlNode].construct.asInstanceOf[Boolean]

  def selectDynamic(field: String): String = getString(field)

  def apply(key: String): Any = map(key).construct

  def getString(key: String): String = map(key).string

  def getStringOption(key: String): Option[String] = map get key map (_.string)

  def getSeq(key: String): Seq[YamlNode] = map(key).seq

  def getSeqOption(key: String): Option[Seq[YamlNode]] = map get key map (_.seq)

  def getBoolean(key: String): Boolean = map(key).boolean

  def contains(key: String): Boolean = map contains key
}

abstract class YamlTag(val name: String) {
  type Rep <: YamlNode
  type Native

  def construct(node: Rep): Native
}

abstract class ScalarYamlTag(name: String) extends YamlTag(name) { type Rep = ScalarYamlNode }

case object StringYamlTag extends ScalarYamlTag("str") {
  type Native = String

  def construct(node: ScalarYamlNode): String = node.scalar
}

case object FloatYamlTag extends ScalarYamlTag("float") {
  type Native = Double

  def construct(node: ScalarYamlNode): Double = node.scalar.toDouble
}

case object IntYamlTag extends ScalarYamlTag("int") {
  type Native = Int

  def construct(node: ScalarYamlNode): Int = node.scalar.toInt
}

case object NullYamlTag extends ScalarYamlTag("null") {
  type Native = Null

  def construct(node: ScalarYamlNode): Null =
    if (node.scalar != "" && node.scalar != "null")
      sys.error(s"invalid !!null tag value representation: ${node.scalar}")
    else null
}

case object BooleanYamlTag extends ScalarYamlTag("bool") {
  type Native = Boolean

  def construct(node: ScalarYamlNode): Boolean =
    node.scalar match {
      case "true"  => true
      case "false" => false
      case s       => sys.error(s"invalid !!bool tag value representation: $s")
    }
}

abstract class ScalarYamlNode(val tag: ScalarYamlTag) extends YamlNode { val scalar: String }

case class StringYamlNode(scalar: String) extends ScalarYamlNode(StringYamlTag)

case object NullYamlNode extends ScalarYamlNode(NullYamlTag) { val scalar: String = "null" }

case class BooleanYamlNode(scalar: String) extends ScalarYamlNode(BooleanYamlTag)

case class IntYamlNode(scalar: String) extends ScalarYamlNode(IntYamlTag)

case object SeqYamlTag extends YamlTag("seq") {
  type Rep = SeqYamlNode
  type Native = List[_]

  def construct(node: SeqYamlNode): Native = node.elems map (_.construct)
}

case class SeqYamlNode(elems: List[YamlNode]) extends YamlNode {
  val tag: SeqYamlTag.type = SeqYamlTag
}

case object MapYamlTag extends YamlTag("map") {
  type Rep = MapYamlNode
  type Native = Map[_, _]

  def construct(node: MapYamlNode): Native = node.entries map { case (k, v) => (k.construct, v.construct) } toMap
}

case class MapYamlNode(entries: List[(YamlNode, YamlNode)]) extends YamlNode {
  val tag: MapYamlTag.type = MapYamlTag
}
