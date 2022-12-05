plugins {
    java

    id("fabric-loom") version "1.0-SNAPSHOT"
    id("io.github.juuxel.loom-quiltflower") version "1.8.0"
    id("org.quiltmc.quilt-mappings-on-loom") version "4.2.1"

    id("com.modrinth.minotaur") version "2.4.4"
    id("com.github.breadmoirai.github-release") version "2.4.1"
}

group = "dev.nyon"
version = "1.0.1"
val authors = listOf("btwonion")
val githubRepo = "btwonion/telekinesis-provider"

repositories {
    mavenCentral()
    maven {
        setUrl("https://cursemaven.com/")
        content {
            includeGroup("curse.maven")
        }
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                setUrl("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:1.19.2")
    mappings(loom.layered {
        //addLayer(quiltMappings.mappings("org.quiltmc:quilt-mappings:1.19.2+build.21:v2"))
        officialMojangMappings()
    })
    modImplementation("net.fabricmc:fabric-loader:0.14.11")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.68.0+1.19.2")

    modImplementation("dev.nyon:telekinesis:1.1.6-1.19.2")
    modApi("curse.maven:tree-harvester-fabric-527685:4074122")
    modApi("maven.modrinth:betterfarmland:1.0.3")
}

tasks {
    processResources {
        val modId = "telekinesis-provider"
        val modName = "Telekinesis Provider"
        val modDescription = "Adds providers for the telekinesis mod"

        inputs.property("id", modId)
        inputs.property("group", project.group)
        inputs.property("name", modName)
        inputs.property("description", modDescription)
        inputs.property("version", project.version)
        inputs.property("github", githubRepo)

        filesMatching(listOf("fabric.mod.json", "quilt.mod.json")) {
            expand(
                "id" to modId,
                "group" to project.group,
                "name" to modName,
                "description" to modDescription,
                "version" to project.version,
                "github" to githubRepo,
            )
        }
    }

    register("releaseMod") {
        group = "publishing"

        dependsOn("modrinth")
        dependsOn("modrinthSyncBody")
        dependsOn("githubRelease")
    }

    withType<JavaCompile> {
        options.release.set(17)
    }
}

modrinth {
    token.set(findProperty("modrinth.token")?.toString())
    projectId.set("Gd0PFfQh")
    versionNumber.set("${project.version}")
    versionType.set("release")
    uploadFile.set(tasks["remapJar"])
    gameVersions.set(listOf("1.19.2"))
    loaders.set(listOf("fabric", "quilt"))
    dependencies {
        required.project("fabric-api")
    }
    syncBodyFrom.set(file("README.md").readText())
}

githubRelease {
    token(findProperty("github.token")?.toString())

    val split = githubRepo.split("/")
    owner(split[0])
    repo(split[1])
    tagName("v${project.version}")
    releaseAssets(tasks["remapJar"].outputs.files)
    targetCommitish("master")
}