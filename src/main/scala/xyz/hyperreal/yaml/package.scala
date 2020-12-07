package xyz.hyperreal

import xyz.hyperreal.char_reader.CharReader

package object yaml {

  def readFromString(s: String): YamlNode =
    new Representation()
      .compose(YamlParser.parseFromString(s))

  private[yaml] def problem(pos: CharReader, error: String): Nothing =
    if (pos eq null)
      sys.error(error)
    else
      pos.error(error)

}
