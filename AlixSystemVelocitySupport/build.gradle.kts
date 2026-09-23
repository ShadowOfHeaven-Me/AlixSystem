import java.net.URI
import org.gradle.api.GradleException

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.6.1"
    //for now disabled
    //id("xyz.kyngs.libby.plugin").version("1.2.1")

    //id("com.guardsquare.proguard-gradle") version "7.6.1"         // ProGuard plugin
    //id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
}

val isUber = false

group = "AlixSystemVelocitySupport"
version = project.findProperty("alix-velocity-version")!!

//The exact Velocity version LINE to compile against - defaults to the latest, but can be overridden per-
//machine via 'velocity-target-version' in your own gradle.properties (e.g. to build against an older,
//more widely-deployed Velocity release for backwards-compatibility testing - pair it with a matching,
//lower 'velocity-toolchain-lang-version' below, since an older Velocity build needs an older JDK too). The
//build NUMBER within whichever version this resolves to is always the newest one PaperMC has published,
//so this never needs bumping just because PaperMC shipped another build of the same version.
val velocityTargetVersion = project.findProperty("velocity-target-version") as? String ?: "4.1.2-SNAPSHOT"

//There's no publicly consumable Maven artifact exposing Velocity's internals (only the much smaller,
//public "velocity-api" is published that way, which doesn't have what this plugin needs to hook into
//Velocity's own netty/channel plumbing) - so this downloads the same server jar PaperMC's downloads page
//itself offers, via their "Fill" API (https://fill.papermc.io/v3/projects/velocity), and uses it as a
//compileOnly file dependency. Already-downloaded builds are cached under the Gradle user home (NOT inside
//this project, so it's never picked up by git) and are never re-downloaded once present - a new PaperMC
//build under 'velocityTargetVersion' simply gets a new file name, which naturally triggers a fresh
//download next time this runs.
fun resolveVelocityJar(): java.io.File {
    val cacheDir = gradle.gradleUserHomeDir.resolve("caches/alix-velocity-jars")
    cacheDir.mkdirs()

    val buildsUrl = "https://fill.papermc.io/v3/projects/velocity/versions/$velocityTargetVersion/builds"
    val json = groovy.json.JsonSlurper()
    @Suppress("UNCHECKED_CAST")
    val builds = try {
        json.parse(URI(buildsUrl).toURL()) as List<Map<*, *>>
    } catch (e: Exception) {
        throw GradleException(
            "Could not fetch Velocity builds for version '$velocityTargetVersion' from $buildsUrl - " +
                    "PaperMC may have stopped publishing that version. Bump 'velocityTargetVersion' in this " +
                    "file (or set 'velocity-target-version' in your own gradle.properties) to a version " +
                    "still listed at https://fill.papermc.io/v3/projects/velocity", e
        )
    }
    if (builds.isEmpty())
        throw GradleException(
            "PaperMC lists no builds at all for Velocity version '$velocityTargetVersion' - " +
                    "bump 'velocityTargetVersion' in this file (or set 'velocity-target-version' in your " +
                    "own gradle.properties) to a version still listed at " +
                    "https://fill.papermc.io/v3/projects/velocity"
        )
    val latestBuild = builds.first()//newest build first, per PaperMC's own ordering
    @Suppress("UNCHECKED_CAST")
    val downloads = latestBuild["downloads"] as Map<*, *>
    val download = downloads["server:default"] as Map<*, *>
    val fileName = download["name"] as String
    val url = download["url"] as String

    val dest = cacheDir.resolve(fileName)
    if (!dest.exists()) {
        logger.lifecycle("Downloading Velocity $velocityTargetVersion build ${latestBuild["id"]} ($fileName) from PaperMC...")
        URI(url).toURL().openStream().use { input: java.io.InputStream -> dest.outputStream().use { output -> input.copyTo(output) } }
    }
    return dest
}

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
    minimize {
        exclude(dependency("org.mariadb.jdbc:mariadb-java-client:.*"))
        exclude(dependency("org.postgresql:postgresql:.*"))
    }
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
    //maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven { url = uri("https://repo.kyngs.xyz/public/") }
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    //geyser/floodgate
    maven("https://repo.opencollab.dev/main")
}

dependencies {
    // https://mvnrepository.com/artifact/com.guardsquare/proguard-gradle
    //runtimeOnly("com.guardsquare:proguard-gradle:7.6.1")


    //implementation("com.alessiodp.libby:libby-velocity:1.3.0")
    //implementation("xyz.kyngs.libby:libby-velocity:1.6.0")

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

    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")

    val srcDir = project.findProperty("velocity-sources-dir")
    //compileOnly(files("$srcDir\\Geyser-Velocity.jar"))
    //compileOnly(files("$srcDir\\floodgate-velocity.jar"))

    compileOnly(files(resolveVelocityJar()))
    /*compileOnly("org.geysermc.geyser:api:2.9.0-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.2.4-SNAPSHOT")*/


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

if (project.findProperty("enable-preview")!! == "true") {
    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("--enable-preview")
    }
    tasks.withType<Test>().configureEach {
        jvmArgs("--enable-preview")
    }

    tasks.withType<JavaExec>().configureEach {
        jvmArgs("--enable-preview")
    }
}

//src/test/java here only holds standalone dev utilities (MessagesMaker, MessagesSyncTool - plain main()
//scripts run manually, never real JUnit tests), so there's nothing for the 'test' task to ever discover -
//without this, a newer Gradle treats that as a failure instead of a harmless no-op.
tasks.test {
    failOnNoDiscoveredTests = false
}

//This module alone can't just use the project's shared 'toolchain-lang-version' (21, matching Spigot's own
//baseline) - PaperMC's own Velocity builds are themselves compiled targeting a newer JDK (build 27 of
//4.1.2-SNAPSHOT ships class file version 69, i.e. Java 25), and an older JDK's javac can't read a newer
//one's class files at all ("class file has wrong version"). Defaults to 25 to match the latest Velocity
//(see velocityTargetVersion above) - override 'velocity-toolchain-lang-version' in your own
//gradle.properties if you've also lowered velocityTargetVersion to an older Velocity release that needs
//an older JDK instead (e.g. 21 alongside a 3.x Velocity version).
java.toolchain.languageVersion.set(JavaLanguageVersion.of(Integer.parseInt(project.findProperty("velocity-toolchain-lang-version") as? String ?: "25")))

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
