plugins {
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
}

base {
    archivesName = properties["archives_base_name"] as String
    version = properties["mod_version"] as String
    group = properties["maven_group"] as String
}

repositories {
    maven {
        name = "meteor-maven"
        url = uri("https://maven.meteordev.org/releases")
    }
    maven {
        name = "meteor-maven-snapshots"
        url = uri("https://maven.meteordev.org/snapshots")
    }
}

loom {
    accessWidenerPath = file("src/main/resources/hontun.accesswidener")
}

sourceSets {
    main {
        java.srcDir("src/mc${properties["minecraft_version"] as String}/java")
        resources.srcDir("src/mc${properties["minecraft_version"] as String}/resources")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"] as String}")
    implementation("net.fabricmc:fabric-loader:${properties["loader_version"] as String}")

    implementation("meteordevelopment:meteor-client:${properties["minecraft_version"] as String}-SNAPSHOT")
}

tasks {
    processResources {
        val propertyMap = mapOf(
            "version" to "${project.version}+${project.property("minecraft_version")}",
            "mc_version" to project.property("minecraft_version"),
        )

        inputs.properties(propertyMap)

        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(propertyMap)
        }
    }

    jar {
        archiveVersion = "${project.version}-${project.property("minecraft_version")}"
        val licenseSuffix = project.base.archivesName.get()
        from("LICENSE") {
            rename { "${it}_${licenseSuffix}" }
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
