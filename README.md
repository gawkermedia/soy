# Scala data structures for Google Closure Templates

This library allows contructing data structures in a similar style how
[Play! Framework's JSON library](http://www.playframework.com/documentation/2.2.1/ScalaJson) allows building JSON,
which can then be directly passed to the
[Play! 2.1 plugin for Google Closure Templates](https://github.com/gawkermedia/play2-closure) for rendering.
The goal is to avoid passing template data as `Map[String, Any]` and use the Scala compiler's support for detecting
mistakes.

## Installation

1. Clone the repository
2. Go into the soy directory
3. Execute `sbt publish-local`
4. Add this to your application as a library dependency:

```scala
libraryDependencies += "com.kinja" %% "soy" % "0.2.2-SNAPSHOT"
```

## Usage

### Template data types

All data types extend a base trait called `SoyValue`. The types described below are direct mappings to
[Google Closure Templates data types](https://developers.google.com/closure/templates/docs/concepts).

#### `SoyNull`

This represents the `null` value.

#### `SoyString`

A regular string.

#### `SoyBoolean`

A regular boolean value of `true` or `false`.

#### `SoyInt`

An integer, represented by an underlying Java `int`.

#### `SoyFloat`

A floating point number, represented by an underlying Java `float`.

#### `SoyDouble`

A double precision floating point number, represented by an underlying Java `double`.

#### `SoyList`

A sequence of `SoyValue`, not necessarily the same types.

#### `SoyMap`

A set of name/value pairs. Names are strings but values can be any `SoyValue`.

### Contructing lists and maps

#### The raw way

You can build lists and maps directly using SoyValue constructors.

```scala
import com.kinja.soy._

SoyMap(Seq(
  "users" -> SoyList(Seq(
    SoyMap(Seq(
      "name" -> SoyString("Bob"),
      "age" -> SoyNumber(31),
      "email" -> SoyString("bob@gmail.com")
    )),
    SoyMap(Seq(
      "name" -> SoyString("Kiki"),
      "age" -> SoyNumber(25),
      "email" -> SoyNull
    ))
  ))
))
```

#### The implicit way

There is an easier way to construct complex data structures using implicit conversions:

```scala
import com.kinja.soy._

Soy.map(
  "users" -> Soy.list(
    Soy.map(
      "name" -> "Bob",
      "age" -> 31,
      "email" -> "bob@gmail.com"
    ),
    Soy.map(
      "name" -> "Kiki",
      "age" -> 25,
      "email" -> SoyNull
    )
  )
)
```

### Creating serializers for your own classes

You can define your own implicit serializers which can be used by the library to convert your classes to `SoyValue`.
Creating such a serializer is as simple as extending the SoyWrites trait and writing the implementation of the `toSoy`
method:

```scala
import com.kinja.soy._

case class User(name: String, age: Int, email: Option[String])

class UserSoyWrites extends SoyWrites[User] {

  def toSoy(user: User): SoyValue = Soy.map(
    "name" -> user.name,
    "age" -> user.age,
    "email" -> user.email
  )
}
```

For the library to be able to use your serializer, you need to make it implicitly available. Here's a more realistic
example which uses [Play! 2.1 plugin for Google Closure Templates](https://github.com/gawkermedia/play2-closure) to
render the template `views.users` in `users.soy` by passing it a `SoyMap` in which the list of users is under the
key `users`. The implicit `UserSoyWrites` is used by the library to convert the `List[User]` to a `SoyList` by
converting each user in it.

```scala
import com.kinja.soy._
import com.kinja.play.plugins.Closure

object App {

  implicit val userSoyWrites = new UserSoyWrites
  
  def render(users: List[User]): String = {
    Closure.render("views.users", Soy.map("users" -> users))
  }
}

```

The `users.soy` template file may look something like this:

```html
{namespace views}

/**
 * A list of users
 * @param users The list of users
 */
{template .users}
{foreach $user in $users}
  <div class="user">
    {$user.name} ({$user.age}) {if $user.email}<a href="mailto:{$user.email}">{$user.email}</a>{/if}
  </div>
{ifempty}
  <div class="message">
    There are no users.
  </div>
{/foreach}
{/template}
```

### Explicitly conversions

You can explicitly convert a value which has an implicit `SoyWrites` availble to `SoyValue` using `Soy.toSoy()`
```scala
import com.kinja.soy._

object App {

  implicit val userSoyWrites = new UserSoyWrites

  val users: List[User] = List(User(...), ...)

  val soyUsers: SoyValue = Soy.toSoy(users)
}
```
