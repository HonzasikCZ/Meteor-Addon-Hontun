pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Umožní Gradle stáhnout JDK 25 pro toolchain, když ho v systému nemáš.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
