package com.blinkbox.books.logging.gelf

import org.scalatest.{Matchers, FunSuite}
import java.io.ByteArrayInputStream
import scala.util.Random

class ChunkTests extends FunSuite with Matchers {

  test("Reads a short stream as a single chunk with no header") {
    val originalData = randomData(100)
    val stream = new ByteArrayInputStream(originalData)
    val chunks = Chunk.readChunks(stream, maxSize = 256).get
    assert(chunks.length == 1)
    assert(!chunks.head.hasHeader)
    assert(body(chunks.head).deep == originalData.deep)
  }

  test("Reads a long stream as multiple chunks with headers") {
    val stream = new ByteArrayInputStream(randomData(1000))
    val chunks = Chunk.readChunks(stream, maxSize = 256).get
    assert(chunks.length == 5) // 12 header bytes per chunk gives us 4 x 244 bytes + 1 x 24 bytes
    assert(chunks.forall(_.hasHeader == true))
  }

  test("Preserves the original data in chunk bodies when using multiple chunks") {
    val originalData = randomData(1000)
    val stream = new ByteArrayInputStream(originalData)
    val chunks = Chunk.readChunks(stream, maxSize = 256).get
    val chunkBodies = chunks.map(body).fold(new Array[Byte](0)) { case (x, y) => x ++ y }
    assert(chunkBodies.deep == originalData.deep)
  }

  test("Starts chunk headers with the correct magic numbers") {
    val stream = new ByteArrayInputStream(randomData(1000))
    val headers = Chunk.readChunks(stream, maxSize = 256).get.map(header)
    assert(headers.forall(h => h(0) == Chunk.MagicNumber1 && h(1) == Chunk.MagicNumber2))
  }

  test("Has the same message identifier in all chunk headers for a single message") {
    val stream = new ByteArrayInputStream(randomData(1000))
    val headers = Chunk.readChunks(stream, maxSize = 256).get.map(header)
    val messageId = headers.head.slice(2, 10)
    assert(headers.forall(_.slice(2, 10).deep == messageId.deep))
  }

  test("Uses the correct index and count in chunk headers") {
    val stream = new ByteArrayInputStream(randomData(1000))
    val headers = Chunk.readChunks(stream, maxSize = 256).get.map(header)
    assert(headers.zipWithIndex.forall { case (h, i) => h(10) == i.asInstanceOf[Byte] })
    assert(headers.forall(h => h(11) == headers.length))
  }

  test("Has different message identifiers for different messages") {
    val stream = new ByteArrayInputStream(randomData(1000))
    val headers1 = Chunk.readChunks(stream, maxSize = 256).get.map(header)
    stream.reset()
    val headers2 = Chunk.readChunks(stream, maxSize = 256).get.map(header)
    assert(headers1.head.slice(2, 10).deep != headers2.head.slice(2, 10).deep)
  }

  def randomData(length: Int): Array[Byte] = {
    val buffer = new Array[Byte](length)
    new Random().nextBytes(buffer)
    buffer
  }

  def header(chunk: Chunk) = chunk.buffer.slice(0, Chunk.HeaderSize)
  def body(chunk: Chunk) = chunk.buffer.slice(Chunk.HeaderSize, chunk.totalLength)

}
