plugins {
  application
  java
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
}

application { mainClass.set("dev.voxcompose.Main") }


tasks.withType<Jar> {
  manifest { attributes["Main-Class"] = "dev.voxcompose.Main" }
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

