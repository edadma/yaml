# YAML Parser for Scala

A lightweight YAML parsing library for Scala that supports JVM, JavaScript, and Native platforms.

## Status

This library is currently in early development (v0.0.1). While some features are not fully implemented according to the YAML specification, the core functionality is working and suitable for many use cases.

## Installation

Add the following dependency to your `build.sbt` file:

```scala
libraryDependencies += "io.github.edadma" %%% "yaml" % "0.0.1"
```

## Basic Usage

### Parsing YAML

To parse a YAML string:

```scala
import io.github.edadma.yaml._

// Parse a YAML string into a YamlNode
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
// Convert the entire YAML structure to native Scala types
val data = node.construct

// The result is a Map[String, Any] for the root object
val typedData = data.asInstanceOf[Map[String, Any]]

// Access values directly using standard Scala map operations
val name = typedData("name").asInstanceOf[String]  // "John Doe"
val age = typedData("age").asInstanceOf[Int]       // 30

// Nested maps are also converted to Map[String, Any]
val address = typedData("address").asInstanceOf[Map[String, Any]]
val city = address("city").asInstanceOf[String]    // "Anytown"

// Lists are converted to List[Any]
val hobbies = typedData("hobbies").asInstanceOf[List[Any]]
val firstHobby = hobbies(0).asInstanceOf[String]   // "reading"
```

This is the approach demonstrated in the test program, which simply prints the constructed data:

```scala
val node = readFromString(yamlString)
pprintln(node.construct)  // Prints the native Scala representation
```

### Accessing Data via YamlNode API

Alternatively, you can access data through the `YamlNode` API:

```scala
// Access scalar values
val name = node.getString("name")  // "John Doe"
val age = node("age")              // Returns the native type (Int in this case)

// Check if a key exists
val hasPhone = node.contains("phone")  // false

// Access nested maps
val city = node.getString("address.city")  // "Anytown"

// Access sequences (lists)
val hobbies = node.getSeq("hobbies")  // Sequence of YamlNodes
val firstHobby = hobbies(0).string    // "reading"

// Dynamic access for strings (using Scala's Dynamic trait)
val nameAlt = node.name  // "John Doe"
```

## Working with Different YAML Types

The library automatically converts YAML values to appropriate Scala types:

```scala
// Strings
val str = node.getString("name")  // "John Doe"

// Numbers
val num = node("age").asInstanceOf[Int]  // 30

// Booleans
val isActive = node.getBoolean("active")  // true/false

// Sequences/Lists
val hobbies = node.getSeq("hobbies")  // List of YamlNodes
val hobbiesNative = node("hobbies").asInstanceOf[List[_]]  // Native Scala List

// Maps/Objects
val address = node.map("address")  // Map of YamlNodes
val addressNative = node("address").asInstanceOf[Map[_, _]]  // Native Scala Map
```

## Type System

The library uses a type system that maps YAML types to Scala types:

| YAML Type | YamlNode Type | Native Scala Type |
|-----------|---------------|-------------------|
| Scalar (string) | StringYamlNode | String |
| Scalar (integer) | IntYamlNode | Int |
| Scalar (float) | FloatYamlNode | Double |
| Scalar (boolean) | BooleanYamlNode | Boolean |
| Scalar (null) | NullYamlNode | null |
| Mapping | MapYamlNode | Map[_, _] |
| Sequence | SeqYamlNode | List[_] |

## Advanced Features

### Type Inference

The library automatically infers types for scalar values:

- Plain scalars are parsed as strings, numbers, booleans, or null based on their content
- Quoted scalars (single or double quotes) are always treated as strings
- Sequences become `List[_]`
- Mappings become `Map[_, _]`

### Working with the Raw YamlNode Tree

You can access the underlying `YamlNode` structure:

```scala
// Check node types
if (node.isMap) {
  // Process as map
}
if (node.isSeq) {
  // Process as sequence
}
```

## Limitations

Current known limitations:

- Some complex YAML features (like anchors, complex tags) may not be fully implemented
- Date/time parsing is not yet fully supported
- Some edge cases in the YAML specification may not be handled correctly

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

// Method 1: Using construct for native Scala types
val node = readFromString(yaml)
val data = node.construct.asInstanceOf[Map[String, Any]]
val users = data("users").asInstanceOf[List[Map[String, Any]]]

for (user <- users) {
  println(s"Name: ${user("name")}")
  println(s"Age: ${user("age")}")
  println("Roles:")
  for (role <- user("roles").asInstanceOf[List[Any]]) {
    println(s"  - $role")
  }
  println()
}

// Method 2: Using YamlNode API
val yamlNode = readFromString(yaml)
val usersNode = yamlNode.getSeq("users")

for (user <- usersNode) {
  println(s"Name: ${user.name}")
  println(s"Age: ${user("age")}")
  println("Roles:")
  for (role <- user.getSeq("roles")) {
    println(s"  - ${role.string}")
  }
  println()
}
```

## Project Information

- Organization: io.github.edadma
- GitHub: https://github.com/edadma/yaml
- License: ISC