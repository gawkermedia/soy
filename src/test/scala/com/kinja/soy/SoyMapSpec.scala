package com.kinja.soy

import org.specs2.mutable._

class SoyMapSpec extends Specification {

  "keys" should {
    "return empty set for empty map" in {
      val map: SoyMap = SoyMap(Seq())
      map.keys === Set()
    }
    "return correct keys for a non-empty map" in {
      val map: SoyMap = SoyMap(Seq(("key1", SoyNull), ("key2", SoyString("hello")), ("key3", SoyInt(12))))
      map.keys === Set("key1", "key2", "key3")
    }
    "not return duplicates" in {
      val map: SoyMap = SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("hello")), ("key2", SoyInt(12))))
      map.keys === Set("key1", "key2")
    }
  }

  "values" should {
    "return empty set for empty map" in {
      val map: SoyMap = SoyMap(Seq())
      map.values === Set()
    }
    "return correct keys for a non-empty map" in {
      val map: SoyMap = SoyMap(Seq(("key1", SoyNull), ("key2", SoyString("hello")), ("key3", SoyInt(12))))
      map.values === Set(SoyNull, SoyString("hello"), SoyInt(12))
    }
    "not return duplicates" in {
      val map: SoyMap = SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("hello")), ("key2", SoyInt(12)), ("key2", SoyNull)))
      map.values === Set(SoyNull, SoyString("hello"), SoyInt(12))
    }
  }

  "++" should {
    "do nothing when appending an empty map" in {
      val map: SoyMap = SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("hello")), ("key2", SoyInt(12)), ("key2", SoyNull)))
      val result = map ++ SoyMap(Seq())
      result === map
    }
    "append to an empty map" in {
      val map: SoyMap = SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("hello")), ("key2", SoyInt(12)), ("key2", SoyNull)))
      val result = SoyMap(Seq()) ++ map
      result === map
    }
    "append values from the new map" in {
      val map: SoyMap = SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("hello")), ("key2", SoyInt(12)), ("key2", SoyNull)))
      val result = map ++ SoyMap(Seq(("key3", SoyString("world")), ("key4", SoyNull)))
      result === SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("hello")), ("key2", SoyInt(12)), ("key2", SoyNull), ("key3", SoyString("world")), ("key4", SoyNull)))
    }
    "replace existing keys from the new map" in {
      val map: SoyMap = SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("hello")), ("key2", SoyInt(12)), ("key2", SoyNull)))
      val result = map ++ SoyMap(Seq(("key2", SoyString("world")), ("key4", SoyNull)))
      result === SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("hello")), ("key2", SoyString("world")), ("key4", SoyNull)))
    }
    "cascade" in {
      val map1: SoyMap = SoyMap(Seq(("key1", SoyString("value1"))))
      val map2: SoyMap = SoyMap(Seq())
      val map3: SoyMap = SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("value2")), ("key2", SoyInt(12))))
      val map4: SoyMap = SoyMap(Seq(("key3", SoyInt(34)), ("key1", SoyString("value4")), ("key5", SoyNull)))
      val result = map1 ++ map2 ++ map3 ++ map4
      result === SoyMap(Seq(("key2", SoyInt(12)), ("key3", SoyInt(34)), ("key1", SoyString("value4")), ("key5", SoyNull)))
    }
  }

  "-" should {
    "not do anything on an empty map" in {
      val map = SoyMap(Seq())
      val result = map - "key1"
      result === map
    }
    "do nothing if the key is not in the map" in {
      val map: SoyMap = SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("value2")), ("key2", SoyInt(12))))
      val result = map - "key3"
      result === map
    }
    "remove the existing key from the map" in {
      val map: SoyMap = SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("value2")), ("key2", SoyInt(12))))
      val result = map - "key2"
      result === SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("value2"))))
    }
    "all occurrences of a key from the map" in {
      val map: SoyMap = SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("value2")), ("key2", SoyInt(12))))
      val result = map - "key1"
      result === SoyMap(Seq(("key2", SoyInt(12))))
    }
    "cascade" in {
      val map: SoyMap = SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("value2")), ("key2", SoyInt(12))))
      val result = map - "key2" - "key1"
      result === SoyMap(Seq())
    }
  }

  "+" should {
    "append to an empty map" in {
      val map = SoyMap(Seq())
      val result = map + ("key1", SoyString("hello"))
      result === SoyMap(Seq(("key1", SoyString("hello"))))
    }
    "append to a map" in {
      val map = SoyMap(Seq(("key1", SoyString("value1"))))
      val result = map + ("key2", SoyString("value2"))
      result === SoyMap(Seq(("key1", SoyString("value1")), ("key2", SoyString("value2"))))
    }
    "append to a map with an existing key" in {
      val map = SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("hello")), ("key2", SoyInt(12)), ("key2", SoyNull)))
      val result = map + ("key2", SoyString("value2"))
      result === SoyMap(Seq(("key1", SoyNull), ("key1", SoyString("hello")), ("key2", SoyInt(12)), ("key2", SoyNull), ("key2", SoyString("value2"))))
    }
    "cascade" in {
      val map = SoyMap(Seq(("key1", SoyString("value1"))))
      val result = map + ("key2", SoyString("value2")) + ("key3", SoyString("value3")) + ("key1", SoyString("value4"))
      result === SoyMap(Seq(("key1", SoyString("value1")), ("key2", SoyString("value2")), ("key3", SoyString("value3")), ("key1", SoyString("value4"))))
    }
  }
}
