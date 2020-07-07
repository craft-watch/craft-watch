package watch.craft.utils

import java.security.MessageDigest

fun String.sha1() = this.toByteArray().sha1()

fun ByteArray.sha1(): String {
  val md = MessageDigest.getInstance("SHA-1")
  md.update(this)
  return md.digest().hex()
}

private fun ByteArray.hex() = joinToString("") { "%02X".format(it) }
