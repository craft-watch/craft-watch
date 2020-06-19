import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED

plugins {
  base
  kotlin("jvm") version "1.3.72"
}

group = "choliver.ipaapi"
version = "0.0.0"

repositories {
  mavenCentral()
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    events(FAILED)
  }
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation("org.jsoup:jsoup:1.13.1")

  testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
  testImplementation("org.hamcrest:hamcrest-library:2.2")
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
  // byte-buddy 1.9.10 (pulled in by Mockito) behaves badly with Java 13 - see https://github.com/mockk/mockk/issues/397
  testImplementation("net.bytebuddy:byte-buddy:1.10.6")
}

