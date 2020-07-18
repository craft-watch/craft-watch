package watch.craft.dsl

import watch.craft.MalformedInputException
import watch.craft.SkipItemException

fun <T, R> T.orSkip(message: String, block: T.() -> R) = try {
  block(this)
} catch (e: MalformedInputException) {
  throw SkipItemException(message)
}

fun <T, R : Any> T.maybeAnyOf(vararg blocks: T.() -> R) = blocks
  .asSequence()
  .mapNotNull { block -> maybe { block(this) } }
  .firstOrNull()

fun <T, R> T.maybe(block: T.() -> R) = try {
  block(this)
} catch (e: MalformedInputException) {
  null
}
