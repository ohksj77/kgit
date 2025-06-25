plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("application")
}

group = "ksj"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    testImplementation(kotlin("test"))
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    archiveBaseName.set("kgit")
    archiveVersion.set("")
    archiveClassifier.set("")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    archiveBaseName.set("kgit")
    archiveVersion.set("")
    archiveClassifier.set("")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}
