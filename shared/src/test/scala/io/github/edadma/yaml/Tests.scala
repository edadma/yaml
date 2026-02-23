package io.github.edadma.yaml

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class Tests extends AnyFreeSpec with Matchers {

  "scalars" - {
    "plain string" in {
      readFromString("hello").construct shouldBe "hello"
    }

    "integer" in {
      readFromString("42").construct shouldBe 42
    }

    "negative integer" in {
      readFromString("-7").construct shouldBe -7
    }

    "float" in {
      readFromString("3.14").construct shouldBe 3.14
    }

    "boolean true" in {
      readFromString("true").construct shouldBe true
    }

    "boolean false" in {
      readFromString("false").construct shouldBe false
    }

    "null value" in {
      readFromString("null") shouldBe NullYamlNode
    }

    "single-quoted string stays string" in {
      readFromString("'42'").construct shouldBe "42"
    }

    "double-quoted string stays string" in {
      readFromString("\"true\"").construct shouldBe "true"
    }
  }

  "block maps" - {
    "simple map" in {
      readFromString("a: 1\nb: 2\n").construct shouldBe Map("a" -> 1, "b" -> 2)
    }

    "string values" in {
      readFromString("name: Alice\ncity: Paris\n").construct shouldBe Map("name" -> "Alice", "city" -> "Paris")
    }

    "mixed value types" in {
      readFromString("x: 1\ny: true\nz: hello\n").construct shouldBe Map("x" -> 1, "y" -> true, "z" -> "hello")
    }
  }

  "block sequences" - {
    "integer list" in {
      readFromString("- 1\n- 2\n- 3\n").construct shouldBe List(1, 2, 3)
    }

    "string list" in {
      readFromString("- a\n- b\n- c\n").construct shouldBe List("a", "b", "c")
    }
  }

  "flow collections" - {
    "flow sequence" in {
      readFromString("[1, 2, 3]").construct shouldBe List(1, 2, 3)
    }

    "flow map" in {
      readFromString("{a: 1, b: 2}").construct shouldBe Map("a" -> 1, "b" -> 2)
    }

    "empty flow map" in {
      readFromString("{}").construct shouldBe Map()
    }

    "empty flow sequence" in {
      readFromString("[]").construct shouldBe List()
    }
  }

  "nested structures" - {
    "map with sequence value" in {
      readFromString("a:\n  - 1\n  - 2\n").construct shouldBe Map("a" -> List(1, 2))
    }

    "sequence of maps" in {
      readFromString("- a: 1\n- b: 2\n").construct shouldBe List(Map("a" -> 1), Map("b" -> 2))
    }

    "nested map" in {
      val s =
        """|a:
           |  b:
           |    - 1
           |    - 2
           |""".stripMargin
      readFromString(s).construct shouldBe Map("a" -> Map("b" -> List(1, 2)))
    }
  }

  "node accessors" - {
    "isMap true" in {
      readFromString("a: 1\n").isMap shouldBe true
    }

    "isMap false for sequence" in {
      readFromString("- 1\n").isMap shouldBe false
    }

    "isSeq true" in {
      readFromString("- 1\n").isSeq shouldBe true
    }

    "isSeq false for map" in {
      readFromString("a: 1\n").isSeq shouldBe false
    }

    "getString" in {
      readFromString("name: Alice\n").getString("name") shouldBe "Alice"
    }

    "getBoolean" in {
      readFromString("flag: true\n").getBoolean("flag") shouldBe true
    }

    "getSeq" in {
      readFromString("items:\n  - 1\n  - 2\n").getSeq("items") should have length 2
    }

    "contains present key" in {
      readFromString("a: 1\nb: 2\n").contains("a") shouldBe true
    }

    "contains absent key" in {
      readFromString("a: 1\nb: 2\n").contains("c") shouldBe false
    }

    "seq length" in {
      readFromString("- 1\n- 2\n- 3\n").seq should have length 3
    }
  }
}
