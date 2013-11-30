package amanuensis.core.neo4j


import scala.concurrent.Future
import scala.util.Try

import amanuensis.core.util.Converters

object Neo4JId {
  import java.net._

  private val maxCounterValue = 16777216
  private val increment = new java.util.concurrent.atomic.AtomicInteger(scala.util.Random.nextInt(maxCounterValue))

  private def counter = (increment.getAndIncrement + maxCounterValue) % maxCounterValue

  private val machineId = {
    val networkInterfacesEnum = NetworkInterface.getNetworkInterfaces
    val networkInterfaces = scala.collection.JavaConverters.enumerationAsScalaIteratorConverter(networkInterfacesEnum).asScala
    val ha = networkInterfaces.find(ha => Try(ha.getHardwareAddress).isSuccess && ha.getHardwareAddress != null && ha.getHardwareAddress.length == 6)
      .map(_.getHardwareAddress)
      .getOrElse(InetAddress.getLocalHost.getHostName.getBytes)
    Converters.md5(ha).take(3)
  }

  def generateId(): String = {
    val timestamp = (System.currentTimeMillis / 1000).toInt

    // n of seconds since epoch. Big endian
    val id = new Array[Byte](12)
    id(0) = (timestamp >>> 24).toByte
    id(1) = (timestamp >> 16 & 0xFF).toByte
    id(2) = (timestamp >> 8 & 0xFF).toByte
    id(3) = (timestamp & 0xFF).toByte

    // machine id, 3 first bytes of md5(macadress or hostname)
    id(4) = machineId(0)
    id(5) = machineId(1)
    id(6) = machineId(2)

    // 2 bytes of the pid or thread id. Thread id in our case. Low endian
    val threadId = Thread.currentThread.getId.toInt
    id(7) = (threadId & 0xFF).toByte
    id(8) = (threadId >> 8 & 0xFF).toByte

    // 3 bytes of counter sequence, which start is randomized. Big endian
    val c = counter
    id(9) = (c >> 16 & 0xFF).toByte
    id(10) = (c >> 8 & 0xFF).toByte
    id(11) = (c & 0xFF).toByte

    Converters.hex2Str(id)
  }
}
