package com.blinkbox.books.logging.gelf

import java.io.InputStream
import java.net.DatagramPacket
import java.security.SecureRandom

private object Chunk {
  private val rng = new SecureRandom()

  val HeaderSize = 12
  val MessageIdSize = 8
  val MagicNumber1 = 0x1e.asInstanceOf[Byte]
  val MagicNumber2 = 0x0f.asInstanceOf[Byte]

  def readChunks(stream: InputStream, maxSize: Int): Seq[Chunk] = {
    val chunks = Iterator.continually(readChunk(stream, maxSize)).takeWhile(!_.isEmpty).toList
    val count = chunks.length
    if (count > 1) {
      val messageId = newMessageId()
      chunks.zipWithIndex.map { case (chunk, index) => chunk.withHeader(messageId, index, count) }
    } else chunks
  }

  private def readChunk(stream: InputStream, maxSize: Int): Chunk = {
    // the chunk is read leaving enough space to put the header in at the start which means we don't
    // need to copy the buffer if the message has multiple chunks. the trade-off is that messages of
    // just under the max chunk size which could be sent as a single chunk will end up being two
    // chunks, but that seems a reasonable trade-off for the general case.
    val buffer = new Array[Byte](maxSize)
    val count = stream.read(buffer, HeaderSize, buffer.length - HeaderSize)
    Chunk(buffer, count)
  }

  private def newMessageId() = {
    val messageId = new Array[Byte](8)
    rng.nextBytes(messageId)
    messageId
  }
}

private case class Chunk(buffer: Array[Byte], messageLength: Int) {
  import Chunk._

  val totalLength = HeaderSize + messageLength
  val isEmpty = messageLength <= 0

  def hasHeader = buffer(0) == MagicNumber1 && buffer(1) == MagicNumber2

  def withHeader(messageId: Array[Byte], index: Int, count: Int): Chunk = {
    assert(messageId.length == MessageIdSize)
    buffer(0) = MagicNumber1
    buffer(1) = MagicNumber2
    System.arraycopy(messageId, 0, buffer, 2, messageId.length)
    buffer(10) = index.asInstanceOf[Byte]
    buffer(11) = count.asInstanceOf[Byte]
    this
  }

  def toPacket =
    if (hasHeader) new DatagramPacket(buffer, 0, totalLength)
    else new DatagramPacket(buffer, HeaderSize, messageLength)
}
