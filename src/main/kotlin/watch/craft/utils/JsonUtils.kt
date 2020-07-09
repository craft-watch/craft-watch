package watch.craft.utils

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun mapper(): ObjectMapper = jacksonObjectMapper()
  .registerModule(JavaTimeModule())
  .enable(INDENT_OUTPUT)
  .disable(WRITE_DATES_AS_TIMESTAMPS)
  .disable(FAIL_ON_UNKNOWN_PROPERTIES)
  .setSerializationInclusion(Include.NON_NULL)  // Our TS convention is that optional values are of type "T | undefined"
