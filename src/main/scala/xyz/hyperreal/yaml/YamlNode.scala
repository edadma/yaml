package xyz.hyperreal.yaml

abstract class YamlNode {
  val tag: YamlTag

  def construct: tag.Native = tag.construct(this.asInstanceOf[tag.RepNode])

  //

  def isSeq: Boolean = isInstanceOf[SeqYamlNode]

  def isMap: Boolean = isInstanceOf[MapYamlNode]

  //

  def seq: Seq[YamlNode] = asInstanceOf[SeqYamlNode].elems

  def map: Map[String, YamlNode] = asInstanceOf[MapYamlNode].entries map { case (k, v) => k.string -> v } toMap

  def string: String = asInstanceOf[StringYamlNode].construct

  def getString(key: String): String = map(key).string

  def getStringOption(key: String): Option[String] = map get key map (_.string)

  def contains(key: String): Boolean = map contains key
}

abstract class YamlTag(val name: String) {
  type RepNode <: YamlNode
  type Native

  def construct(node: RepNode): Native
}

abstract class ScalarYamlTag(name: String) extends YamlTag(name) { type RepNode = ScalarYamlNode }

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

case class ScalarYamlNode(tag: ScalarYamlTag, scalar: String) extends YamlNode

case class StringYamlNode(scalar: String) extends YamlNode { val tag: StringYamlTag.type = StringYamlTag }

case object NullYamlNode extends YamlNode { val tag: NullYamlTag.type = NullYamlTag }

case object SeqYamlTag extends YamlTag("seq") {
  type RepNode = SeqYamlNode
  type Native = List[_]

  def construct(node: SeqYamlNode): Native = node.elems map (_.construct)
}

case class SeqYamlNode(elems: List[YamlNode]) extends YamlNode {
  val tag: SeqYamlTag.type = SeqYamlTag
}

case object MapYamlTag extends YamlTag("map") {
  type RepNode = MapYamlNode
  type Native = Map[_, _]

  def construct(node: MapYamlNode): Native = node.entries map { case (k, v) => (k.construct, v.construct) } toMap
}

case class MapYamlNode(entries: List[(YamlNode, YamlNode)]) extends YamlNode {
  val tag: MapYamlTag.type = MapYamlTag
}
