plugins {
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.9-SNAPSHOT" apply false
    java
}

architectury {
    minecraft = property("minecraft_version").toString()
}

allprojects {
    group = property("maven_group").toString()
    version = property("mod_version").toString()
}

val javaRelease: Int = (rootProject.findProperty("java_release") as? String)?.toIntOrNull() ?: 21
val javaVersion: JavaVersion = JavaVersion.toVersion(javaRelease.toString())

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(javaRelease)
    }

    if (name == "helper" || name == "common") {
        repositories { mavenCentral() }
        return@subprojects
    }

    apply(plugin = "architectury-plugin")

    repositories {
        mavenCentral()
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

tasks.register("buildAll") {
    dependsOn(":fabric:build", ":forge:build", ":neoforge:build")
    doLast {
        println("=== OpenFriend mod loaders built ===")
    }
}
