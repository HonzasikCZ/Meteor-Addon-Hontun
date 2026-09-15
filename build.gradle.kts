plugins {
    // NOVÝ loom variant bez remappingu (pro deobfuskovanou 26.x). Krátké "fabric-loom"
    // je starý variant, co vyžaduje mappings -> proto padal "mappings has no dependencies".
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

dependencies {
    // 26.x se distribuuje DEOBFUSKOVANĚ (jména v jaru jsou už čitelná), takže se
    // NEDEKLARUJE žádný mappings() - proto ho template nemá a officialMojangMappings()
    // selhává ("Failed to find official mojang mappings"): není žádný mapping soubor ke stažení.
    minecraft("com.mojang:minecraft:${properties["minecraft_version"] as String}")
    implementation("net.fabricmc:fabric-loader:${properties["loader_version"] as String}")

    // Meteor
    implementation("meteordevelopment:meteor-client:${properties["minecraft_version"] as String}-SNAPSHOT")
}

tasks {
    processResources {
        val propertyMap = mapOf(
            "version" to project.version,
            "mc_version" to project.property("minecraft_version"),
        )

        inputs.properties(propertyMap)

        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(propertyMap)
        }
    }

    jar {
        val licenseSuffix = project.base.archivesName.get()
        from("LICENSE") {
            rename { "${it}_${licenseSuffix}" }
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

// MC 26.x -> Java 25. Toolchain umožní Gradle běžet na jiné JDK (třeba 21) a kompilovat na 25;
// foojay resolver v settings.gradle.kts umí JDK 25 stáhnout, pokud ho v systému nemáš.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
