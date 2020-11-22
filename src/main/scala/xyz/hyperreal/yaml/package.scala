package xyz.hyperreal

import xyz.hyperreal.char_reader.CharReader

package object yaml {

  def readFromString(s: String): Any = readFromSource(io.Source.fromString(s))

  def readFromSource(src: io.Source): Any = readFromCharReader(CharReader.fromSource(src))

  def readFromCharReader(r: CharReader): Any = {}

  private[yaml] def problem(pos: CharReader, error: String): Nothing =
    if (pos eq null)
      sys.error(error)
    else
      pos.error(error)

}
