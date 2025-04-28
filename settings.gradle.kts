rootProject.name = "AlixSystem"
include("AlixSystemSpigot")
include("AlixSystemLoader")
include("AlixSystemVelocitySupport")
include("AlixAPI")
include("AlixAPI:AlixAPISpigot")
findProject(":AlixAPI:AlixAPISpigot")?.name = "AlixAPISpigot"
