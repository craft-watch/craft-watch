plugins {
  kotlin("js")
}

group = "choliver.neapi"
version = "0.0.0"

repositories {
  jcenter() // Because of kotlinx.html
}

dependencies {
  implementation(kotlin("stdlib-js"))
  implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.1")
}

kotlin.target.browser {}
