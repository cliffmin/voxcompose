plugins {
  application
  java
  jacoco
}

group = "dev.voxcompose"
version = "0.1.0"

java {
  toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories { mavenCentral() }

dependencies {
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("com.google.code.gson:gson:2.11.0")
  
  // Testing dependencies
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
  testImplementation("org.mockito:mockito-core:5.11.0")
  testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
  testImplementation("org.assertj:assertj-core:3.25.3")
}

application { mainClass.set("dev.voxcompose.Main") }


tasks.withType<Jar> {
  manifest { attributes["Main-Class"] = "dev.voxcompose.Main" }
}

// Configure test task
tasks.test {
  useJUnitPlatform()
  testLogging {
    events("passed", "skipped", "failed")
    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    showStandardStreams = false
  }
}

// Configure JaCoCo coverage
tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
    html.required.set(true)
    csv.required.set(false)
  }
}

tasks.jacocoTestCoverageVerification {
  violationRules {
    rule {
      limit {
        minimum = "0.8".toBigDecimal()
      }
    }
  }
}

// Build a fat JAR without Shadow plugin (compatible with Gradle 9)
val fatJar = tasks.register<Jar>("fatJar") {
  archiveClassifier.set("all")
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  from(sourceSets.main.get().output)
  dependsOn(configurations.runtimeClasspath)
  from({
    configurations.runtimeClasspath.get()
      .filter { it.name.endsWith(".jar") }
      .map { zipTree(it) }
  })
  manifest { attributes["Main-Class"] = "dev.voxcompose.Main" }
}

