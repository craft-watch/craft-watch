package watch.craft

import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun mapper() = jacksonObjectMapper()
  .registerModule(JavaTimeModule())
  .enable(INDENT_OUTPUT)
  .disable(WRITE_DATES_AS_TIMESTAMPS)
