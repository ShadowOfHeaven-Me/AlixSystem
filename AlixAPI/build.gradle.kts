plugins {
    id("java")
    id("java-library")
    id("com.gradleup.shadow") version "8.3.2"
}

group = "AlixAPI"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    //kyori
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots"
    }
    //packetevents
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    //netty
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.retrooper:packetevents-netty-common:${project.findProperty("packet-events-version")}")
    compileOnly("io.netty:netty-all:4.1.24.Final")
    compileOnly("net.kyori:adventure-api:4.18.0")

    compileOnlyApi("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    implementation("org.jetbrains:annotations:24.0.1")
    annotationProcessor("org.jetbrains:annotations:24.0.1")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    //compileOnly(project(":AlixSystemLoader"))
}

tasks.test {
    useJUnitPlatform()
}