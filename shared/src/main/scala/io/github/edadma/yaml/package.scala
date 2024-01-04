package io.github.edadma.yaml

import io.github.edadma.char_reader.CharReader

def readFromString(s: String): YamlNode =
  new Representation()
    .compose(YamlParser.parseFromString(s))

private[yaml] def problem(pos: CharReader, error: String): Nothing =
  if (pos eq null)
    sys.error(error)
  else
    pos.error(error)
