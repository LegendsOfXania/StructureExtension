plugins {
    /* Kotlin */
    kotlin("jvm") version "2.2.10"
    /* Typewriter */
    id("com.typewritermc.module-plugin") version "2.1.0"
}

group = "fr.legendsofxania"
version = "0.0.1"

repositories {}
dependencies {}

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
        engineVersion = "0.9.0-beta-167"
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA

        paper()
    }
}

kotlin {
    jvmToolchain(21)
}