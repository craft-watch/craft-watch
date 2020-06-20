import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED

plugins {
  kotlin("multiplatform") version "1.3.72"
  kotlin("plugin.serialization") version "1.3.72"
}

group = "choliver.neapi"
version = "0.0.0"

repositories {
  jcenter() // Because of kotlinx.html
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
  testLogging {
    events(FAILED)
  }
}

kotlin {
  jvm()
  js().browser {
    webpackTask {
      sourceMaps = false
    }
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(kotlin("stdlib-common"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.20.0")
      }
    }

    jvm().compilations["main"].defaultSourceSet {
      dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation("org.jsoup:jsoup:1.13.1")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
      }
    }

    jvm().compilations["test"].defaultSourceSet {
      dependencies {
        implementation("org.junit.jupiter:junit-jupiter:5.5.2")
        implementation("org.hamcrest:hamcrest-library:2.2")
        implementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
        // byte-buddy 1.9.10 (pulled in by Mockito) behaves badly with Java 13 - see https://github.com/mockk/mockk/issues/397
        implementation("net.bytebuddy:byte-buddy:1.10.6")
      }
    }

    js().compilations["main"].defaultSourceSet {
      dependencies {
        implementation(kotlin("stdlib-js"))
        implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.1")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0")
      }
    }
  }
}
