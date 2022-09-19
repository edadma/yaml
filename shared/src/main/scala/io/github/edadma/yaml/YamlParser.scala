package io.github.edadma.yaml

import io.github.edadma.char_reader.CharReader
import io.github.edadma.char_reader.CharReader.{DEDENT, INDENT}
import io.github.edadma.pattern_matcher.Matchers

import scala.language.{implicitConversions, postfixOps}

object YamlParser extends Matchers[CharReader] {

  trait Context

  case object BlockOut

  case object BlockIn

  case object `flow-out` extends Context

  case object `flow-in` extends Context

  case object `block-key` extends Context

  case object `flow-key` extends Context

  override def keyword(s: String): Matcher[String] = null

  override implicit def str(s: String): Matcher[String] =
    whitespace ~> super.str(s) <~ whitespace

  override def space: Matcher[Char] = anyOf(' ', '\t')

  override def lineComment: Matcher[_] = '#'

  override def blockCommentStart: Matcher[_] = not(pos)

  override def blockCommentEnd: Matcher[_] = not(pos)

  def s(s: String): Matcher[String] = super.str(s)

  implicit def set(pred: Char => Boolean): Matcher[Char] = elem(pred)

  val /*[1]*/ `c-printable`: Set[Char] = Set('\t', '\r', '\n') ++ (' ' to '~')

  val /*[3]*/ `c-byte-order-mark` = '\uFEFF'

  val /*[22]*/ `c-indicator` =
    Set('-', '?', ':', ',', '[', ']', '{', '}', '#', '&', '*', '!', '|', '>', '\'', '"', '%', '@', '`')

  val /*[23]*/ `c-flow-indicator` = Set(',', '[', ']', '{', '}')

  val /*[26]*/ `b-char` = Set('\r', '\n')

  val /*[27]*/ `nb-char`: Set[Char] = `c-printable` -- `b-char` - `c-byte-order-mark`

  val /*[33]*/ `s-white` = Set(' ', '\t')

  val /*[34]*/ `ns-char`: Set[Char] = `nb-char` -- `s-white`

  def /*[126]*/ `ns-plain-first`(c: Context): Matcher[Char] =
    ((`ns-char` -- `c-indicator`): Matcher[Char]) | (ch('?') | ':' | '-') <~ guard(`ns-plain-safe`(c))

  def /*[127]*/ `ns-plain-safe`(c: Context): Set[Char] =
    c match {
      case `flow-out`  => `ns-plain-safe-out`
      case `flow-in`   => `ns-plain-safe-in`
      case `block-key` => `ns-plain-safe-out`
      case `flow-key`  => `ns-plain-safe-in`
    }

  val /*[128]*/ `ns-plain-safe-out`: Set[Char] = `ns-char`
  val /*[129]*/ `ns-plain-safe-in`: Set[Char] = `ns-char` -- `c-flow-indicator`

  def /*[130]*/ `ns-plain-char`(c: Context): Matcher[Char] =
    elem(`ns-plain-safe`(c) - ':' - '#') | ':' <~ guard(`ns-plain-safe`(c)) // todo: still incomplete

  def /*[132]*/ `nb-ns-plain-in-line`(c: Context): Matcher[List[List[Char] ~ Char]] =
    (`s-white`.* ~ `ns-plain-char`(c)) *

  def /*[133]*/ `ns-plain-one-line`(c: Context): Matcher[String] =
    string(`ns-plain-first`(c) ~ `nb-ns-plain-in-line`(c))

  def anchor: Matcher[String] = '&' ~> string(rep1(letterOrDigit))

  def tag: YamlParser.Matcher[String] = '!' ~> '!' ~> string(rep1(letter))

  def input: Matcher[ValueAST] =
    matchall(whitespace ~ eoi ^^^ EmptyAST | repu(nl) ~> documentValue)

  def nl: Matcher[_] = whitespace ~ (eoi | guard(DEDENT) | rep1('\n'))

  def nl1: Matcher[_] = eoi | guard(DEDENT) | '\n'

  def documentValue: Matcher[ValueAST] =
    blockPairs ^^ (p => MapAST(None, None, p)) |
      blockListElements ^^ (l => SeqAST(None, None, l)) |
      flowValue(`flow-in`) |
      multiline

  def blockContainer: Matcher[ContainerAST] = blockMap | blockList

  def blockMap: Matcher[MapAST] =
    opt(anchor) ~ opt(tag) ~ (nl ~> INDENT ~> blockPairs <~ DEDENT) ^^ {
      case a ~ t ~ p => MapAST(a, t, p)
    }

  def blockPairs: Matcher[List[(ValueAST, ValueAST)]] = rep1(blockPair)

  def blockPair: Matcher[(ValueAST, ValueAST)] =
    flowValue(`flow-out`) ~ ":" ~ blockValue ^^ {
      case k ~ _ ~ v => (k, v)
    } |
      complexKey ~ opt(nl ~ ":" ~ opt(blockValue)) ^^ {
        case k ~ (None | Some(_ ~ _ ~ None)) => (k, EmptyAST)
        case k ~ Some(_ ~ _ ~ Some(v))       => (k, v)
      }

  def complexKey: Matcher[ValueAST] =
    "?" ~> blockListElement ~ opt(INDENT ~> blockListElements <~ DEDENT) ^^ {
      case v ~ None     => SeqAST(None, None, List(v))
      case v ~ Some(vs) => SeqAST(None, None, v :: vs)
    } |
      "?" ~> blockValue

  def blockList: Matcher[SeqAST] =
    opt(anchor) ~ opt(tag) ~ (nl ~> INDENT ~> blockListElements <~ DEDENT) ^^ {
      case a ~ t ~ p => SeqAST(a, t, p)
    }

  // todo: 184 incomplete
  def /*[184]*/ blockListElements: Matcher[List[ValueAST]] = rep1(blockListElement)

  def blockListElement: YamlParser.Matcher[ValueAST] = '-' ~> not(`ns-char`) ~> whitespace ~> blockListValue

  def blockListValue: Matcher[ValueAST] =
    blockListElement ~ (INDENT ~> blockListElements <~ DEDENT) ^^ {
      case p ~ ps => SeqAST(None, None, p :: ps)
    } |
      blockListElement ^^ (p => SeqAST(None, None, List(p))) |
      blockPair ~ (INDENT ~> blockPairs <~ DEDENT) ^^ {
        case p ~ ps => MapAST(None, None, p :: ps)
      } |
      blockPair ^^ (p => MapAST(None, None, List(p))) |
      blockValue

  def blockValue: Matcher[ValueAST] = blockContainer | optNull(flowValue(`flow-out`)) <~ nl | multiline

  // todo: 173 incomplete
  def /*[173]*/ `l-literal-content`: Matcher[String] = string(elem(`nb-char`).*)

  def multiline: Matcher[ValueAST] =
    opt(anchor) ~ opt(tag) ~ ("|" | "|-") ~ (nl ~> INDENT ~> affect(_.textUntilDedent()) ~> rep1(
      not(DEDENT) ~> `l-literal-content` <~ nl1) <~ DEDENT) ^^ {
      case a ~ t ~ "|" ~ l => PlainAST(a, t, l.mkString("", "\n", "\n"))
      case a ~ t ~ _ ~ l   => PlainAST(a, t, l mkString "\n")
    } |
      opt(anchor) ~ opt(tag) ~ opt(">" | ">-") ~ (INDENT ~> rep1(affect(_.textUntilDedent()) <~ nl) <~ DEDENT) ^^ {
        case a ~ t ~ Some(">") ~ l => PlainAST(a, t, l mkString ("", " ", "\n"))
        case a ~ t ~ _ ~ l         => PlainAST(a, t, l mkString " ")
      }

  def orNull(a: Option[ValueAST]): ValueAST =
    a match {
      case None    => EmptyAST
      case Some(v) => v
    }

  def flowContainer: Matcher[ContainerAST] = flowMap | flowList

  def flowMap: Matcher[MapAST] =
    opt(anchor) ~ opt(tag) ~ ('{' ~> repsep(flowPair, ",") <~ '}') ^^ {
      case a ~ t ~ l => MapAST(a, t, l)
    }

  def flowPair: Matcher[(ValueAST, ValueAST)] =
    flowValue(`flow-in`) ~ opt(":" ~> optNull(flowValue(`flow-in`))) ^^ {
      case k ~ None    => (k, EmptyAST)
      case k ~ Some(v) => (k, v)
    }

  def flowList: Matcher[ContainerAST] =
    opt(anchor) ~ opt(tag) ~ ('[' <~ ']' | '[' ~> repsep(optNull(flowValue(`flow-in`)), ",") <~ ']') ^^ {
      case a ~ t ~ '['                 => SeqAST(a, t, Nil)
      case a ~ t ~ (l: List[ValueAST]) => SeqAST(a, t, l)
    }

  def optNull[T <: ValueAST](m: Matcher[T]): Matcher[ValueAST] = opt(m) ^^ orNull

  def flowValue(c: Context): Matcher[ValueAST] = flowContainer | primitive(c)

  def flowPlainText(c: Context): Matcher[String] = `ns-plain-one-line`(c)

  def primitive(c: Context): Matcher[ValueAST] =
    opt(anchor) ~ opt(tag) ~ (singleStringLit ^^ (s => ('s', s)) | doubleStringLit ^^ (s => ('d', s)) | flowPlainText(c) ^^ (
        s => ('p', s))) ^^ {
      case a ~ t ~ (('s', s)) => SingleQuotedAST(a, t, s)
      case a ~ t ~ (('d', s)) => DoubleQuotedAST(a, t, s)
      case a ~ t ~ ((_, s))   => PlainAST(a, t, s)
    }

  def parseFromString(s: String): ValueAST =
    parseFromCharReader(CharReader.fromString(s, indentation = Some(("#", "", ""))))

  def parseFromCharReader(r: CharReader): ValueAST =
    input(r) match {
      case Match(result, _) => result
      case m: Mismatch      => m.error
    }

}

//      opt(anchor) <~ "null" ^^^ NullAST |
//      opt(anchor) <~ "true" ^^ (BooleanAST(_, v = true)) |
//      opt(anchor) <~ "false" ^^ (BooleanAST(_, v = true)) |
//      opt(anchor) <~ ".inf" ^^ (NumberAST(_, v = Double.PositiveInfinity)) |
//      opt(anchor) <~ "-.inf" ^^ (NumberAST(_, v = Double.NegativeInfinity)) |
//      opt(anchor) <~ ".nan" ^^ (NumberAST(_, v = Double.NaN)) |
