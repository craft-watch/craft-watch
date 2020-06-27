package choliver.neapi.storage

interface HtmlCache {
  fun write(key: String, text: String)
  fun read(key: String): String?
}
