plugins {
  kotlin("js")
  kotlin("plugin.serialization") version "1.3.72"
}

group = "choliver.neapi"
version = "0.0.0"

repositories {
  jcenter() // Because of kotlinx.html
}

dependencies {
  implementation(kotlin("stdlib-js"))
  implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.1")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0")
}

kotlin.target.browser {}
