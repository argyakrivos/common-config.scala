package com.blinkbox.books.logging.gelf

import ch.qos.logback.core.{AppenderBase, Layout}
import java.io.ByteArrayInputStream
import java.net.{DatagramSocket, InetAddress}
import java.nio.charset.StandardCharsets.UTF_8
import java.util.zip.DeflaterInputStream
import scala.beans.BeanProperty
import scala.util.control.NonFatal

object UdpAppender {
  val DefaultChunkSize = 512
  val DefaultPort = 12201
}

class UdpAppender[T] extends AppenderBase[T] {
  import UdpAppender._

  private var socket: DatagramSocket = null

  @BeanProperty var layout: Layout[T] = null
  @BeanProperty var maxChunkSize: Int = DefaultChunkSize
  @BeanProperty var host: String = null
  @BeanProperty var port: Int = DefaultPort

  override def start() {
    if (layout == null) addError(s"No layout set for $name")
    if (maxChunkSize < 256 || maxChunkSize > 8192) addError(s"$name has invalid chunk size")

    socket = new DatagramSocket()
    try socket.connect(InetAddress.getByName(host), port)
    catch {
      case NonFatal(e) => addError(s"$name failed to connect to $host:$port", e)
    }

    super.start()
  }

  override def stop() {
    socket.close()
  }

  def append(event: T) {
    val layoutData = new ByteArrayInputStream(layout.doLayout(event).getBytes(UTF_8))
    val chunks = Chunk.readChunks(new DeflaterInputStream(layoutData), maxChunkSize)
    chunks.map(_.toPacket).foreach(socket.send)
  }
}
