package choliver.neapi

import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File

fun main() {
  val getter = HttpGetter(CACHE_DIR)
  val executor = Executor(getter)
  val mapper = jacksonObjectMapper().enable(INDENT_OUTPUT)

  File("src/jsMain/resources/inventory.json").outputStream().use { ostream ->
    mapper.writeValue(ostream, executor.scrapeAll())
  }
}
