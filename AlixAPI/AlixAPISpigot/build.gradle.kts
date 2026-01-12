plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.0"
    `maven-publish`
}

group = "AlixSystem"
version = "1.0.0"

tasks.build {
    actions.clear()
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier.set("")
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "com.github.ShadowOfHeaven-Me"
            artifactId = "AlixSystem"
            version = "3.6.1"
        }
    }
}
repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    //packetevents
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    implementation(project(":AlixAPI"))

    //compileOnly("org.spigotmc:spigot:1.20.2-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:${project.findProperty("packet-events-version")}")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}