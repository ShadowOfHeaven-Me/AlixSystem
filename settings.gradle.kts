rootProject.name = "AlixSystem"
//include("src")
include("AlixSystemSpigot")
include("AlixSystemLoader")
include("AlixSystemVelocitySupport")
include("AlixAPI")
include("AlixAPI:AlixAPISpigot")
findProject(":AlixAPI:AlixAPISpigot")?.name = "AlixAPISpigot"
//include(":Velocity")
//project(":Velocity").projectDir = file("C:\\Users\\Kubia\\Desktop\\client\\eclipse\\Velocity")
/*dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}*/
/*
sourceControl {
    gitRepository(URI("https://github.com/PaperMC/Velocity.git")) {
        producesModule("Velocity:proxy")
    }
}*/
