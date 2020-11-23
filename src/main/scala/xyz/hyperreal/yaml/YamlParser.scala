package xyz.hyperreal.yaml

import xyz.hyperreal.char_reader.CharReader
import xyz.hyperreal.char_reader.CharReader.{DEDENT, INDENT}
import xyz.hyperreal.pattern_matcher.Matchers

object YamlParser extends Matchers[CharReader] {

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

  val /*[126]*/ `ns-plain-first`: Set[Char] = `ns-char` -- `c-indicator` // todo: incomplete

  val /*[129]*/ `ns-plain-safe-in`: Set[Char] = `ns-char` -- `c-flow-indicator`

  def /*[130]*/ `ns-plain-char`: Matcher[Char] = elem(`ns-plain-safe-in` - ':' - '#') // todo: incomplete

  def /*[132]*/ `nb-ns-plain-in-line`: Matcher[List[List[Char] ~ Char]] =
    (`s-white`.* ~ `ns-plain-char`) *

  def /*[133]*/ `ns-plain-one-line`: Matcher[String] = string(`ns-plain-first` ~ `nb-ns-plain-in-line`)

  def anchor: Matcher[String] = '&' ~> string(rep1(letterOrDigit))

  def tag: YamlParser.Matcher[String] = '!' ~> '!' ~> string(rep1(letter))

  def input: Matcher[ValueAST] = matchall(repu(nl) ~> documentValue)

  def nl: Matcher[_] = whitespace ~ (eoi | guard(DEDENT) | rep1('\n'))

  def nl1: Matcher[_] = eoi | guard(DEDENT) | '\n'

  def documentValue: Matcher[ValueAST] =
    blockPairs ^^ (p => MapAST(None, None, p)) |
      blockListElements ^^ (l => SeqAST(None, None, l)) |
      flowValue |
      multiline

  def blockContainer: Matcher[ContainerAST] = blockMap | blockList

  def blockMap: Matcher[MapAST] =
    opt(anchor) ~ opt(tag) ~ (nl ~> INDENT ~> blockPairs <~ DEDENT) ^^ {
      case a ~ t ~ p => MapAST(a, t, p)
    }

  def blockPairs: Matcher[List[(ValueAST, ValueAST)]] = rep1(blockPair)

  def blockPair: Matcher[(ValueAST, ValueAST)] =
    flowValue ~ ":" ~ blockValue ^^ {
      case k ~ _ ~ v => (k, v)
    }
  /*|
      complexKey ~ opt(nl ~ ":" ~ opt(value)) ^^ {
        case k ~ (None | Some(_ ~ _ ~ None)) => (k, NullAST)
        case k ~ Some(_ ~ _ ~ Some(v))       => (k, v)
      }*/

//  def complexKey: Matcher[ValueAST] =
//    "?" ~> "-" ~> opt(listValue) ~ opt(INDENT ~> listValues <~ DEDENT) ^^ {
//      case v ~ None     => SeqAST(None, List(ornull(v)))
//      case v ~ Some(vs) => SeqAST(None, ornull(v) :: vs)
//    } |
//      "?" ~> value

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

  def blockValue: Matcher[ValueAST] = blockContainer | optNull(flowValue) <~ nl | multiline

  // todo: 173 incomplete
  def /*[173]*/ `l-literal-content`: Matcher[String] = string(elem(`nb-char`) *)

  def multiline: Matcher[ValueAST] =
    opt(anchor) ~ opt(tag) ~ ("|" | "|-") ~ (nl ~> INDENT ~> affect(_.textUntilDedent()) ~> rep1(
      not(DEDENT) ~> `l-literal-content` <~ nl1) <~ DEDENT) ^^ {
      case a ~ t ~ "|" ~ l  => PlainAST(a, t, l.mkString("", "\n", "\n"))
      case a ~ t ~ "|-" ~ l => PlainAST(a, t, l mkString "\n")
    } /*|
      opt(anchor) ~ opt(">" | ">-") ~ (INDENT ~> rep1(textLit <~ nl) <~ DEDENT) ^^ {
        case a ~ Some(">") ~ l => StringAST(a, l mkString ("", " ", "\n"))
        case a ~ _ ~ l         => StringAST(a, l mkString " ")
      }*/

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
    flowValue ~ opt(":" ~> optNull(flowValue)) ^^ {
      case k ~ None    => (k, EmptyAST)
      case k ~ Some(v) => (k, v)
    }

  def flowList: Matcher[ContainerAST] =
    opt(anchor) ~ opt(tag) ~ ('[' ~> repsep(optNull(flowValue), ",") <~ ']') ^^ {
      case a ~ t ~ l => SeqAST(a, t, l)
    }

  def optNull[T <: ValueAST](m: Matcher[T]): Matcher[ValueAST] = opt(m) ^^ orNull

  def flowValue: Matcher[ValueAST] = flowContainer | primitive

  def flowPlainText: Matcher[String] = `ns-plain-one-line`

  def primitive: Matcher[ValueAST] =
    opt(anchor) ~ opt(tag) ~ (singleStringLit ^^ (s => ('s', s)) | doubleStringLit ^^ (s => ('d', s)) | flowPlainText ^^ (
        s => ('p', s))) ^^ {
      case a ~ t ~ (('s', s)) => SingleQuotedAST(a, t, s)
      case a ~ t ~ (('d', s)) => DoubleQuotedAST(a, t, s)
      case a ~ t ~ (('p', s)) => PlainAST(a, t, s)
    }

  def parseFromString(s: String): ValueAST =
    parseFromCharReader(CharReader.fromString(s, indentation = Some((Some("#"), None))))

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
