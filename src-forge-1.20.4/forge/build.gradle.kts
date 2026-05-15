plugins {
    id("dev.architectury.loom")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    accessWidenerPath.set(project(":common-mc").file("src/main/resources/openfriend.accesswidener"))
    forge {
        convertAccessWideners.set(true)
    }
}

configurations {
    create("common") { isCanBeResolved = true }
    create("shadowCommon") { isCanBeResolved = true }
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
}

val mcVersion: String = project.property("minecraft_version").toString()
val forgeVersion: String = project.property("forge_version").toString()

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())
    "forge"("net.minecraftforge:forge:$forgeVersion")

    "common"(project(path = ":common-mc", configuration = "namedElements")) { isTransitive = false }
    "shadowCommon"(project(path = ":common-mc", configuration = "transformProductionForge")) { isTransitive = false }

    implementation(project(":common"))
    implementation(project(":helper"))
    "shadowCommon"(project(":common")) { isTransitive = false }
    "shadowCommon"(project(":helper")) { isTransitive = false }
}

tasks.named<Jar>("jar") {
    archiveClassifier.set("dev")
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    exclude("architectury.common.json")
    configurations = listOf(project.configurations["shadowCommon"])
    archiveClassifier.set("dev-shadow")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    inputFile.set(tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").flatMap { it.archiveFile })
    dependsOn("shadowJar")
    archiveClassifier.set("forge")
}
