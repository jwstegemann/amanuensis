/*
 * Copyright 2013 Stephane Godbillon (@sgodbillon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package amanuensis.core.util

import scala.language.postfixOps

//FIXME: take this from reactiveMongo if included

/** Common functions */
object Converters {
  private val HEX_CHARS: Array[Char] = "0123456789abcdef".toCharArray();

  /** Turns an array of Byte into a String representation in hexadecimal. */
  def hex2Str(bytes: Array[Byte]): String = {
    val hex = new Array[Char](2 * bytes.length)
    var i = 0
    while (i < bytes.length) {
      hex(2 * i) = HEX_CHARS((bytes(i) & 0xF0) >>> 4)
      hex(2 * i + 1) = HEX_CHARS(bytes(i) & 0x0F)
      i = i + 1
    }
    new String(hex)
  }

  /** Turns a hexadecimal String into an array of Byte. */
  def str2Hex(str: String): Array[Byte] = {
    val bytes = new Array[Byte](str.length / 2)
    var i = 0
    while (i < bytes.length) {
      bytes(i) = Integer.parseInt(str.substring(2 * i, 2 * i + 2), 16).toByte
      i += 1
    }
    bytes
  }

  /** Computes the MD5 hash of the given String. */
  def md5(s: String) = java.security.MessageDigest.getInstance("MD5").digest(s.getBytes)

  /** Computes the MD5 hash of the given Array of Bytes. */
  def md5(array: Array[Byte]) = java.security.MessageDigest.getInstance("MD5").digest(array)

  /** Computes the MD5 hash of the given String and turns it into a hexadecimal String representation. */
  def md5Hex(s: String): String = hex2Str(md5(s))

  private val digest = java.security.MessageDigest.getInstance("SHA-256")

  private def sha(s: String): String = {
    val m = digest.digest(s.getBytes("UTF-8"));
    m map { c => (c & 0xff) toHexString } mkString
  }
}