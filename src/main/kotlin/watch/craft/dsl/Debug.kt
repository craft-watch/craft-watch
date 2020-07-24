package watch.craft.dsl

import watch.craft.utils.mapper

fun Any.printAsJson() = println(mapper().writeValueAsString(this))
