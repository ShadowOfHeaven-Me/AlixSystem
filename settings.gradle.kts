pluginManagement {
    repositories {
        maven {
            url = uri("https://repo.kyngs.xyz/gradle-plugins")
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.opencollab.dev/main")
        gradlePluginPortal()
    }
}
rootProject.name = "AlixSystem"
include("AlixSystemSpigot")
include("AlixSystemLoader")
include("AlixSystemVelocitySupport")
include("AlixAPI")
include("AlixAPI:AlixAPISpigot")
findProject(":AlixAPI:AlixAPISpigot")?.name = "AlixAPISpigot"
