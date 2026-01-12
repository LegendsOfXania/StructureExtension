plugins {
    /* Kotlin */
    kotlin("jvm") version "2.2.10"
    /* Typewriter */
    id("com.typewritermc.module-plugin") version "2.1.0"
    /* Paperweight */
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    /* Maven Publish */
    `maven-publish`
}

group = "fr.legendsofxania"
version = "0.0.1"

repositories {}
dependencies {
    /* Paperweight */
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

typewriter {
    namespace = "legendsofxania"

    extension {
        name = "Structure"
        shortDescription = "Display schematics in Typewriter."
        description = """
            Display schematics in your interactions and create
            beautiful places directly in Typewriter.
            Created by the Legends of Xania.
            """.trimMargin()
        engineVersion = "0.9.0-beta-170"
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA

        paper()
    }
}

kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String

            from(components["java"])
        }
    }
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION