plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.2"
    id("xyz.kyngs.libby.plugin").version("1.2.1")
    //id("com.guardsquare.proguard-gradle") version "7.6.1"         // ProGuard plugin
    //id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
}

val isUber = false

group = "AlixSystemVelocitySupport"
version = project.findProperty("alix-velocity-version")!!

tasks.build {
    actions.clear()
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    destinationDirectory = file(project.findProperty("velocity-build-dir") as String)
    archiveBaseName.set("AlixVelocity")
    archiveClassifier.set("")//w pizde z z tym "-all" suffixem
    val prefix = "alix.libs"
    relocate("io.github.retrooper.packetevents", "$prefix.io.github.retrooper.packetevents")
    relocate("com.github.retrooper.packetevents", "$prefix.com.github.retrooper.packetevents")
    //relocate("net.kyori", "$prefix.net.kyori")
    relocate("com.alessiodp.libby", "$prefix.com.alessiodp.libby")

    //if (!isUber)
    minimize()
}

repositories {
    //mavenLocal()
    mavenCentral()
    //geyser/floodgate
    maven("https://repo.opencollab.dev/main/")

    maven("https://jitpack.io/")
    maven("https://mvnrepository.com/artifact/com.guardsquare/proguard-gradle")

    //kyori
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    //packetevents
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    //libby
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    // https://mvnrepository.com/artifact/com.guardsquare/proguard-gradle
    //runtimeOnly("com.guardsquare:proguard-gradle:7.6.1")
    compileOnly("com.alessiodp.libby:libby-velocity:2.0.0-SNAPSHOT")
    if (isUber) {
        compileOnly(project(":AlixSystemLoader"))
        implementation(project(":AlixSystemSpigot"))
    } else {
        implementation(project(":AlixSystemLoader"))
    }


    /*compileOnly("Velocity:proxy") {
        version {
            branch = "dev/3.0.0"
        }
    }*/
    //velocity already uses caffeine
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.0")

    var srcDir = project.findProperty("velocity-sources-dir")
    compileOnly(files("$srcDir\\Geyser-Velocity.jar"))
    compileOnly(files("$srcDir\\floodgate-velocity.jar"))
    compileOnly(files("$srcDir\\velocity-3.4.0-SNAPSHOT-469.jar"))


    //implementation("com.velocitypowered:velocity:3.4.0-SNAPSHOT")
    //compileOnly("com.velocitypowered:velocity:3.4.0-SNAPSHOT")

    //compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    implementation("com.github.retrooper:packetevents-velocity:${project.findProperty("packet-events-version")}")
    compileOnly("net.kyori:adventure-api:4.14.0")
    compileOnly("net.kyori:adventure-nbt:4.14.0")
    //compileOnly(project(":Velocity"))

    //annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    compileOnly("io.netty:netty-all:4.1.24.Final")
}
/*
publishing {
    publications {
        create<MavenPublication>("velocityJar") {
            groupId = "com.velocitypowered"
            artifactId = "velocity"
            version = "3.4.0-SNAPSHOT"
            artifact(file("C:/Users/Kubia/Desktop/alix sources/velocity/velocity-3.4.0-SNAPSHOT-469.jar"))
        }
    }
}*/
