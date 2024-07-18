pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.polyfrost.cc/releases")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net")
        maven("https://maven.minecraftforge.net/")
        maven("https://repo.spongepowered.org/maven/")
        maven("https://repo.sk1er.club/repository/maven-releases/")
        maven("https://repo.essential.gg/repository/maven-public/")
        maven("https://jitpack.io/")
    }
    plugins {
        kotlin("jvm") version "2.0.0"
    }
}

rootProject.name = "MightyMinerV2"