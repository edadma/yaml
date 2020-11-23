package xyz.hyperreal

import xyz.hyperreal.char_reader.CharReader

package object yaml {

  def readFromString(s: String): YamlNode = readFromSource(io.Source.fromString(s))

  def readFromFile(s: String): YamlNode = readFromSource(io.Source.fromFile(s))

  def readFromSource(src: io.Source): YamlNode =
    new Representation()
      .compose(YamlParser.parseFromCharReader(CharReader.fromSource(src, indentation = Some((Some("#"), None)))))

  private[yaml] def problem(pos: CharReader, error: String): Nothing =
    if (pos eq null)
      sys.error(error)
    else
      pos.error(error)

}
