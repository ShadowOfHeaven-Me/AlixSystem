plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.2"
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
repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    //packetevents
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])
            groupId = "com.github.ShadowOfHeaven-Me"
            artifactId = "AlixSystem"
            version = project.version as String
        }
    }
}

dependencies {
    implementation(project(":AlixAPI"))

    //compileOnly("org.spigotmc:spigot:1.20.2-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.7.1-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}