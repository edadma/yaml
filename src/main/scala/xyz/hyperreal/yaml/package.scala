package xyz.hyperreal

import xyz.hyperreal.char_reader.CharReader

package object yaml {

  def readFromString(s: String): Any = readFromSource(io.Source.fromString(s))

  def readFromFile(s: String): Any = readFromSource(io.Source.fromFile(s))

  def readFromSource(src: io.Source): Any =
    new Evaluator(Nil)
      .eval(YamlParser.parseFromCharReader(CharReader.fromSource(src, indentation = Some((Some("#"), None)))))

  private[yaml] def problem(pos: CharReader, error: String): Nothing =
    if (pos eq null)
      sys.error(error)
    else
      pos.error(error)

}
