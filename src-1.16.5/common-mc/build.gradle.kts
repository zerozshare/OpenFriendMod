plugins {
    id("dev.architectury.loom")
}

architectury {
    common("fabric", "forge", "neoforge")
}

val mcVersion: String = project.property("minecraft_version").toString()
val javaRelease: String = project.property("java_release").toString()

loom {
    accessWidenerPath.set(file("src/main/resources/openfriend.accesswidener"))
    mixin {
        defaultRefmapName.set("openfriend.refmap.json")
    }
}

repositories {
    maven("https://repo.spongepowered.org/maven/")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())
    compileOnly("org.spongepowered:mixin:0.8.7")

    implementation(project(":common"))
    implementation(project(":helper"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(javaRelease)
    targetCompatibility = JavaVersion.toVersion(javaRelease)
}

tasks.withType<JavaCompile> {
    options.release.set(javaRelease.toInt())
}
