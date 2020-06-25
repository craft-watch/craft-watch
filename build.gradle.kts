import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.3.72"
  application
}

group = "choliver.neapi"
version = "0.0.0"

repositories {
  jcenter() // Because of kotlinx.html
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation("io.github.microutils:kotlin-logging:1.7.10")
  implementation("org.jsoup:jsoup:1.13.1")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.0")
  runtimeOnly("ch.qos.logback:logback-classic:1.2.3")

  testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
  testImplementation("org.hamcrest:hamcrest-library:2.2")
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
  // byte-buddy 1.9.10 (pulled in by Mockito) behaves badly with Java 13 - see https://github.com/mockk/mockk/issues/397
  testImplementation("net.bytebuddy:byte-buddy:1.10.6")
}

application {
  mainClassName = "choliver.neapi.ApplicationKt"
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "1.8"
  }
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    events(FAILED)
  }
}
