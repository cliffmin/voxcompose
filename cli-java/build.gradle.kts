plugins {
    application
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.voxcompose"
version = "0.4.2"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.voxcompose.cli.Main")
}

// Configure manifest so --version can print Implementation-Version
// Apply to all Jar-like tasks, including ShadowJar

tasks.withType<org.gradle.jvm.tasks.Jar>().configureEach {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to "voxcompose-cli",
                "Implementation-Version" to project.version.toString()
            )
        )
    }
}

// Configure fat JAR (shadow) for distribution
// Avoid imports in Kotlin DSL by fully qualifying the ShadowJar task type

// Configure all ShadowJar tasks (provided by the shadow plugin)
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    archiveClassifier.set("all")
}

// Convenience alias
tasks.register("fatJar") {
    dependsOn("shadowJar")
}
