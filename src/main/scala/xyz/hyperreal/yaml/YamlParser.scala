package xyz.hyperreal.yaml

import xyz.hyperreal.char_reader.CharReader
import xyz.hyperreal.char_reader.CharReader.{DEDENT, INDENT}
import xyz.hyperreal.pattern_matcher.Matchers

object YamlParser extends Matchers[CharReader] {

  override def keyword(s: String): Matcher[String] = null

  override implicit def str(s: String): Matcher[String] =
    whitespace ~> super.str(s) <~ whitespace

  def s(s: String): Matcher[String] = super.str(s)

  val /*[1]*/ `c-printable`: Set[Char] = Set('\t', '\r', '\n') ++ (' ' to '~')

  val /*[3]*/ `c-byte-order-mark` = '\uFEFF'

  val /*[22]*/ `c-indicator` =
    Set('-', '?', ':', ',', '[', ']', '{', '}', '#', '&', '*', '!', '|', '>', '\'', '"', '%', '@', '`')

  val /*[23]*/ `c-flow-indicator` = Set(',', '[', ']', '{', '}')

  val /*[26]*/ `b-char` = Set('\r', '\n')

  val /*[27]*/ `nb-char`: Set[Char] = `c-printable` -- `b-char` - `c-byte-order-mark`

  val /*[33]*/ `s-white` = Set(' ', '\t')

  val /*[34]*/ `ns-char`: Set[Char] = `nb-char` -- `s-white`

  val /*[126]*/ `ns-plain-first`: Set[Char] = `ns-char` -- `c-indicator` // todo: incomplete

  val /*[129]*/ `ns-plain-safe-in`: Set[Char] = `ns-char` -- `c-flow-indicator`

  def /*[130]*/ `ns-plain-char`: YamlParser.Matcher[Char] = elem(`ns-plain-safe-in` - ':' - '#') // todo: incomplete

  def /*[132]*/ `nb-ns-plain-in-line`: YamlParser.Matcher[List[List[Char] ~ Char]] =
    (elem(`s-white`).* ~ `ns-plain-char`) *

  def /*[133]*/ `ns-plain-one-line`: YamlParser.Matcher[String] = string(elem(`ns-plain-first`) ~ `nb-ns-plain-in-line`)

  def anchor: Matcher[String] = '&' ~> string(rep1(letterOrDigit))

  def input: Matcher[ValueAST] = matchall(documentValue)

  def nl: Matcher[_] = rep1('\n')

  def onl: Matcher[_] = rep('\n')

  def documentValue: Matcher[ValueAST] =
    pairs ^^ (p => MapAST(None, p)) |
      listValues ^^ (l => ListAST(None, l)) //|
//    flowValue /*|
//  multiline */

  def flowPlainText: Matcher[String] = `ns-plain-one-line`

  def container: Matcher[ContainerAST] = map | list

  def map: Matcher[MapAST] =
    opt(anchor) ~ (INDENT ~> pairs <~ DEDENT) ^^ {
      case a ~ p => MapAST(a, p)
    }

  def pairs: Matcher[List[(ValueAST, ValueAST)]] =
    rep1(pair <~ nl)

  def pair: Matcher[(ValueAST, ValueAST)] =
    flowValue ~ ":" ~ opt(value) ^^ {
      case k ~ _ ~ v => (k, ornull(v))
    } |
      complexKey ~ opt(nl ~ ":" ~ opt(value)) ^^ {
        case k ~ (None | Some(_ ~ _ ~ None)) => (k, NullAST)
        case k ~ Some(_ ~ _ ~ Some(v))       => (k, v)
      }

  def complexKey: Matcher[ValueAST] =
    "?" ~> "-" ~> opt(listValue) ~ opt(INDENT ~> listValues <~ DEDENT) ^^ {
      case v ~ None     => ListAST(None, List(ornull(v)))
      case v ~ Some(vs) => ListAST(None, ornull(v) :: vs)
    } |
      "?" ~> value

  def list: Matcher[ListAST] =
    opt(anchor) ~ (INDENT ~> listValues <~ DEDENT) ^^ {
      case a ~ p => ListAST(a, p)
    }

  def listValues: Matcher[List[ValueAST]] =
    rep1("-" ~> opt(listValue) <~ nl) ^^ (l => l map ornull)

  def listValue: Matcher[ValueAST] =
    pair ~ (INDENT ~> pairs <~ DEDENT) ^^ {
      case p ~ ps => MapAST(None, p :: ps)
    } |
      pair ^^ (p => MapAST(None, List(p))) |
      value

  def value: Matcher[ValueAST] =
    primitive | container | flowContainer //| multiline

  def ornull(a: Option[ValueAST]): ValueAST =
    a match {
      case None    => NullAST
      case Some(v) => v
    }

  def flowContainer: Matcher[ContainerAST] = flowMap | flowList

  def flowMap: Matcher[MapAST] =
    opt(anchor) ~ ("{" ~> repsep(flowPair, ",") <~ "}") ^^ {
      case a ~ l => MapAST(a, l)
    }

  def flowPair: Matcher[(ValueAST, ValueAST)] =
    flowValue ~ opt(":" ~ opt(flowValue)) ^^ {
      case k ~ None        => (k, NullAST)
      case k ~ Some(_ ~ v) => (k, ornull(v))
    }

  def flowList: Matcher[ContainerAST] =
    opt(anchor) ~ ("[" ~> repsep(opt(flowValue), ",") <~ "]") ^^ {
      case a ~ l => ListAST(a, l map ornull)
    }

  def flowValue: Matcher[ValueAST] = primitive | flowContainer

  def primitive: Matcher[PrimitiveAST] =
    opt(anchor) ~ (singleStringLit | doubleStringLit) ^^ { case a ~ s => StringAST(a, s) } |
      opt(anchor) ~ (floatLit | integerLit) ^^ { case a ~ n           => NumberAST(a, n.asInstanceOf[Number]) } |
      opt(anchor) <~ "null" ^^^ NullAST |
      opt(anchor) <~ "true" ^^ (BooleanAST(_, v = true)) |
      opt(anchor) <~ "false" ^^ (BooleanAST(_, v = true)) |
      opt(anchor) <~ ".inf" ^^ (NumberAST(_, v = Double.PositiveInfinity)) |
      opt(anchor) <~ "-.inf" ^^ (NumberAST(_, v = Double.NegativeInfinity)) |
      opt(anchor) <~ ".nan" ^^ (NumberAST(_, v = Double.NaN)) |
      opt(anchor) ~ flowPlainText ^^ { case a ~ s => StringAST(a, s) }

  def parseFromString(s: String): ValueAST =
    input(CharReader.fromString(s, indentation = Some((Some("#"), None)))) match {
      case Match(result, _) => result
      case m: Mismatch      => m.error
    }

}
