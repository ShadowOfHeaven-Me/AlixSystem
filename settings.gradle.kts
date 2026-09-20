pluginManagement {
    repositories {
        maven {
            url = uri("https://repo.kyngs.xyz/gradle-plugins")
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.opencollab.dev/main")
        gradlePluginPortal()
    }
    plugins {
        kotlin("jvm") version "2.4.20"
    }
}

//Lets Gradle download a matching JDK on its own (via the Foojay Disco API) whenever a required toolchain
//version (e.g. 21, see 'toolchain-lang-version' in gradle.properties) isn't already installed locally -
//without this, a missing JDK fails with "Toolchain download repositories have not been configured"
//instead of just being fetched automatically.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "AlixSystem"
include("AlixSystemSpigot")
include("AlixSystemLoader")
include("AlixSystemVelocitySupport")
include("AlixAPI")
include("AlixAPI:AlixAPISpigot")
findProject(":AlixAPI:AlixAPISpigot")?.name = "AlixAPISpigot"
