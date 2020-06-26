package choliver.neapi.getters

import java.io.File

fun Getter<String>.toHtml() = HtmlGetter(this)
fun Getter<String>.cached(cacheDir: File) = CachingGetter(cacheDir, this)
