rootProject.name = "AlixSystem"
//include("src")
include("AlixSystemSpigot")
include("AlixSystemLoader")
include("AlixAPI")
include("AlixAPI:AlixAPISpigot")
findProject(":AlixAPI:AlixAPISpigot")?.name = "AlixAPISpigot"
