# Scala data structures for Google Closure Templates

This library allows contructing data structures in a similar style how Play! Framework's JSON allows building JSON,
which can then be directly passed to the `Play! 2.1 plugin for Google Closure Templates` for rendering. The goal is to
avoid passing template data as `Map[String, Any]` and use the Scala compiler's support for detecting mistakes.

## Install

1. Clone the repository
2. Go into the soy directory
3. Execute `sbt publish-local`
4. Add this to your application as a librarydependency:

```scala
libraryDependencies += "com.kinja" %% "soy" % "0.1-SNAPSHOT"
```

## Usage

TODO
