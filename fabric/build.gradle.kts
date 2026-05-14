plugins {
    id("dev.architectury.loom")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath.set(project(":common-mc").file("src/main/resources/openfriend.accesswidener"))
}

configurations {
    create("common") { isCanBeResolved = true }
    create("shadowCommon") { isCanBeResolved = true }
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
}

val mcVersion: String = project.property("minecraft_version").toString()
val fabricLoaderVersion: String = project.property("fabric_loader_version").toString()

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")

    "common"(project(path = ":common-mc", configuration = "namedElements")) { isTransitive = false }
    "shadowCommon"(project(path = ":common-mc", configuration = "transformProductionFabric")) { isTransitive = false }

    implementation(project(":common"))
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version.toString(),
        "minecraft_version" to mcVersion
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}

tasks.named<Jar>("jar") {
    archiveClassifier.set("dev")
    from(project(":common").sourceSets["main"].output)
    from(project(":common-mc").sourceSets["main"].output)
    from(project(":helper").sourceSets["main"].output)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("fabric")
}
