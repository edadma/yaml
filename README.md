# yaml

![Maven Central](https://img.shields.io/maven-central/v/io.github.edadma/yaml_sjs1_3)
[![Last Commit](https://img.shields.io/github/last-commit/edadma/yaml)](https://github.com/edadma/yaml/commits)
![GitHub](https://img.shields.io/github/license/edadma/yaml)
![Scala Version](https://img.shields.io/badge/Scala-3.8.1-blue.svg)
![ScalaJS Version](https://img.shields.io/badge/Scala.js-1.20.2-blue.svg)
![Scala Native Version](https://img.shields.io/badge/Scala_Native-0.5.10-blue.svg)

A lightweight YAML parsing library for Scala that supports JVM, JavaScript, and Native platforms.

## Status

This library is currently in early development. While some features are not fully implemented according to the YAML specification, the core functionality is working and suitable for many use cases.

## Installation

Add the following dependency to your `build.sbt` file:

```scala
libraryDependencies += "io.github.edadma" %%% "yaml" % "0.0.2"
```

## Prerequisites

- JDK 11 or higher
- sbt 1.12.1 or higher
- Node.js (for JavaScript platform)
- LLVM/Clang (for Native platform)

## Basic Usage

### Parsing YAML

To parse a YAML string:

```scala
import io.github.edadma.yaml._

val yamlString = """
name: John Doe
age: 30
address:
  street: 123 Main St
  city: Anytown
  zip: 12345
hobbies:
  - reading
  - hiking
  - coding
"""

val node = readFromString(yamlString)
```

### Converting to Native Scala Types

The most straightforward way to use the parsed data is to convert it to native Scala types using the `construct` method:

```scala
val data = node.construct.asInstanceOf[Map[String, Any]]

val name = data("name").asInstanceOf[String]  // "John Doe"
val age  = data("age").asInstanceOf[Int]       // 30

val address = data("address").asInstanceOf[Map[String, Any]]
val city = address("city").asInstanceOf[String]    // "Anytown"

val hobbies = data("hobbies").asInstanceOf[List[Any]]
val firstHobby = hobbies(0).asInstanceOf[String]   // "reading"
```

### Accessing Data via YamlNode API

```scala
val name    = node.getString("name")         // "John Doe"
val age     = node("age")                    // Returns the native type (Int)
val hasPhone = node.contains("phone")        // false

val hobbies    = node.getSeq("hobbies")      // Seq[YamlNode]
val firstHobby = hobbies(0).string           // "reading"

// Dynamic access for strings (using Scala's Dynamic trait)
val nameAlt = node.name  // "John Doe"
```

## Working with Different YAML Types

The library automatically converts YAML values to appropriate Scala types:

```scala
val str      = node.getString("name")               // "John Doe"
val num      = node("age").asInstanceOf[Int]         // 30
val isActive = node.getBoolean("active")             // true/false
val hobbies  = node.getSeq("hobbies")               // Seq[YamlNode]
val address  = node("address").asInstanceOf[Map[_, _]]
```

## Type System

| YAML Type        | YamlNode Type   | Native Scala Type |
|------------------|-----------------|-------------------|
| Scalar (string)  | StringYamlNode  | String            |
| Scalar (integer) | IntYamlNode     | Int               |
| Scalar (float)   | FloatYamlNode   | Double            |
| Scalar (boolean) | BooleanYamlNode | Boolean           |
| Scalar (null)    | NullYamlNode    | null              |
| Mapping          | MapYamlNode     | Map[_, _]         |
| Sequence         | SeqYamlNode     | List[_]           |

## Type Inference

- Plain scalars are inferred as string, integer, float, boolean, or null based on their content
- Quoted scalars (single or double quotes) are always treated as strings
- Sequences become `List[_]`
- Mappings become `Map[_, _]`

## Node Type Checks

```scala
if (node.isMap) { /* process as map */ }
if (node.isSeq) { /* process as sequence */ }
```

## Building and Testing

```bash
# Compile all platforms
sbt compile

# Run tests on all platforms
sbt test

# Run tests on a specific platform
sbt yamlJVM/test
sbt yamlJS/test
sbt yamlNative/test
```

## Publishing

```bash
sbt publishSigned
sbt sonatypeBundleRelease
```

## Complete Example

```scala
import io.github.edadma.yaml._

val yaml = """
users:
  - name: Alice
    age: 28
    roles:
      - admin
      - editor
  - name: Bob
    age: 35
    roles:
      - viewer
"""

val node = readFromString(yaml)

// Using construct
val data  = node.construct.asInstanceOf[Map[String, Any]]
val users = data("users").asInstanceOf[List[Map[String, Any]]]

for (user <- users) {
  println(s"Name: ${user("name")}")
  println(s"Age: ${user("age")}")
  for (role <- user("roles").asInstanceOf[List[Any]])
    println(s"  - $role")
}

// Using YamlNode API
for (user <- node.getSeq("users")) {
  println(s"Name: ${user.name}")
  println(s"Age: ${user("age")}")
  for (role <- user.getSeq("roles"))
    println(s"  - ${role.string}")
}
```

## Limitations

- Anchors and aliases are not yet fully implemented
- Date/time parsing is not yet fully supported
- Some edge cases in the YAML specification may not be handled

## License

This project is licensed under the ISC License - see the [LICENSE](LICENSE) file for details.
