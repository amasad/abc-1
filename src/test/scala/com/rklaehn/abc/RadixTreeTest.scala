package com.rklaehn.abc

import spire.implicits._
import org.junit.Assert._
import org.junit.Test
import spire.util.Opt

class RadixTreeTest {

  implicit class StringOps(underlying: String) {
    def toBytes = underlying.getBytes("UTF-8")
  }

  val kvs = (0 until 100).map(i => i.toString -> i)

  val kvs1k = (0 to 1000).map(i => i.toString -> i)

  val tree = RadixTree(kvs: _*)

  val tree1k = RadixTree(kvs1k :_*)

  val bkvs = (0 until 100).map(i => i.toString.toBytes -> i.toString.toBytes)

  val btree = RadixTree(bkvs: _*)

  val bkvs1k = (0 to 1000).map(i => i.toString.toBytes -> i.toString.toBytes)

  val btree1k = RadixTree(bkvs1k: _*)

  @Test
  def testEquals(): Unit = {
    assertEquals(tree, RadixTree(kvs: _*))
    assertEquals(tree, RadixTree(kvs.reverse: _*))
    assertEquals(btree, RadixTree(bkvs: _*))
    assertEquals(btree, RadixTree(bkvs.reverse: _*))
    assertFalse(tree == "fnord")
  }

  @Test
  def testHashCode(): Unit = {
    assertEquals(tree.##, RadixTree(kvs: _*).##)
    assertEquals(btree.##, RadixTree(bkvs: _*).##)
  }

  @Test
  def testToString(): Unit = {
    assertEquals("RadixTree(1 -> 1)", RadixTree("1" -> 1).toString)
    assertFalse(RadixTree("1" -> 1).printStructure.isEmpty)
  }

  @Test
  def testStartsWith(): Unit = {
    assertTrue(RadixTree("1" -> 1).startsWith("1"))
    assertTrue(RadixTree("11" -> 1).startsWith("1"))
    assertFalse(RadixTree("11" -> 1).startsWith("2"))
    assertFalse(RadixTree("1" -> 1).startsWith("11"))
  }

  @Test
  def testEmptyIsEmpty(): Unit = {
    assertTrue(RadixTree.empty[String, Int].isEmpty)
    assertTrue(RadixTree.empty[Array[Byte], Array[Byte]].isEmpty)
  }

  @Test
  def testContains(): Unit = {
    assertEquals(kvs.size, tree.count)
    for(i <- 0 until 100)
      assertEquals(i, tree(i.toString))
    assertFalse(tree.contains("101"))
    assertFalse(tree.contains("-1"))
    assertFalse(RadixTree("a" -> 1).contains("b"))
  }

  @Test
  def testMergeCollision(): Unit = {
    val a = RadixTree("a" -> 1)
    val b = a.merge(a, _ + _)
    assertEquals(2, b("a"))
  }

  @Test
  def testPairs(): Unit = {
    assertEquals(
      kvs.toSet,
      tree.pairs.toSet)
  }

  @Test
  def testPairsByteArray(): Unit = {
    import spire.implicits._
    assertTrue(btree.pairs.toArray === RadixTree(bkvs: _*).pairs.toArray)
  }

  @Test
  def testKeys(): Unit = {
    assertEquals(
      kvs.map(_._1).toSet,
      tree.keys.toSet)
  }

  @Test
  def testValues(): Unit = {
    assertEquals(
      kvs.map(_._2).toSet,
      tree.values.toSet)
  }

  @Test
  def testFilterPrefix(): Unit = {
    assertEquals(
      kvs.filter { case (k,v) => k.startsWith("1")}.toSeq,
      tree.filterPrefix("1").pairs.toSeq)
    assertTrue(RadixTree("1" -> 1).filterPrefix("foo").isEmpty)
  }

  @Test
  def testModifyOrRemove(): Unit = {
    val tree1 = tree.modifyOrRemove { case (k,v,_) => Opt(v * 2) }
    for((k,v)<- kvs)
      assertEquals(v*2, tree1(k))
  }

  @Test
  def testSubtreeWithPrefix(): Unit = {
    assertTrue(tree.subtreeWithPrefix("x").isEmpty)
    assertEquals(11,tree.subtreeWithPrefix("1").count)
  }

  @Test
  def testFilter(): Unit = {
    assertEquals(
      kvs.filter { case (k,v) => k.startsWith("1")}.toSeq,
      tree.filter { case (k,v) => k.startsWith("1")}.pairs.toSeq)
  }

  @Test
  def testFilterKeysContaining(): Unit = {
    val tt = RadixTree("aa" -> 1, "ab" -> 2, "a" -> 3)
    assertEquals(1,tt.filterKeysContaining("aa").count)
    assertEquals(0,tt.filterKeysContaining("aaa").count)
    val t = RadixTree("a" -> 1)
    assertTrue(t.filterKeysContaining("b").isEmpty)
    assertFalse(t.filterKeysContaining("a").isEmpty)
    assertEquals(
      kvs1k.filter(_._1.contains("10")).size,
      tree1k.filterKeysContaining("10").count
    )
    assertEquals(
      kvs.filter(_._1.contains("1")).size,
      tree.filterKeysContaining("1").count
    )
  }

  @Test
  def testFilterKeysContainingBytes(): Unit = {
    assertEquals(
      kvs1k.filter(_._1.contains("10")).size,
      btree1k.filterKeysContaining("10".toBytes).count
    )
    assertEquals(1, RadixTree("abcd".toBytes -> 1).filterKeysContaining("bcd".toBytes).count)
  }
}