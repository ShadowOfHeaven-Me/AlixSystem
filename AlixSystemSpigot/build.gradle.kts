plugins {//java-library
    id("java")
    id("java-library")
    id("com.gradleup.shadow") version "8.3.2"
}

group = "AlixSystemSpigot"
version = "${project.findProperty("alix-spigot-version")}"

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.build {
    actions.clear()
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    destinationDirectory = file(project.findProperty("build-dir") as String)
    archiveBaseName.set("AlixSystem")
    archiveClassifier.set("")//w pizde z z tym "-all" suffixem
    val prefix = "alix.libs"
    var list = listOf(
        "io.github.retrooper.packetevents",
        "com.github.retrooper.packetevents",
        "net.kyori.adventure.api",
        "net.kyori.adventure.nbt"
    )

    for (s in list) {
        relocate(s, "$prefix.$s")
    }

    /*relocate("io.github.retrooper.packetevents", "$prefix.io.github.retrooper.packetevents")
    relocate("com.github.retrooper.packetevents", "$prefix.com.github.retrooper.packetevents")
    relocate("net.kyori:adventure-api", "$prefix.net.kyori:adventure-api")
    relocate("net.kyori:adventure-nbt", "$prefix.net.kyori:adventure-nbt")*/
    minimize()
}

/*tasks.build {
    dependsOn(tasks["spigot0"])
}*/

repositories {
    mavenCentral()
    mavenLocal()
    //kyori
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots"
    }
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    //packetevents
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    //paper
    maven("https://repo.papermc.io/repository/maven-public/")
}
tasks.register("prepareKotlinBuildScriptModel") {}
dependencies {
    compileOnly("com.google.code.gson:gson:2.9.0")
    implementation(project(":AlixSystemLoader"))
    implementation(project(":AlixAPI:AlixAPISpigot"))

    implementation("com.github.retrooper:packetevents-spigot:${project.findProperty("packet-events-version")}") {
        //exclude("net.kyori", "adventure-api")
        //exclude("net.kyori", "adventure-nbt")
    }

    implementation("net.kyori:adventure-api:4.19.0")
    implementation("net.kyori:adventure-nbt:4.19.0")

    compileOnlyApi("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    // https://mvnrepository.com/artifact/org.jetbrains/annotations
    //implementation("org.jetbrains:annotations:15.0")
    //implementation(project(":common"))
    //var srcPath = "C:\\Users\\Kubia\\Desktop\\alix sources\\spigot"
    //implementation(files("$srcPath\\asm-9.7.jar"))
    //implementation(files("$srcPath\\asm-tree-9.7.jar"))
    //implementation(files("$srcPath\\byte-buddy-agent-1.14.18.jar"))
    //implementation(files("$srcPath\\paperlib-1.0.7.jar"))
    //can't find the internals
    //compileOnly(files("$srcPath\\Geyser-Spigot.jar"))

    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-tree:9.7")
    implementation("net.bytebuddy:byte-buddy-agent:1.14.18")
    implementation("io.papermc:paperlib:1.0.7")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot:1.20.2-R0.1-SNAPSHOT")

    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
}

configurations.all {
    resolutionStrategy {
        force("com.google.code.gson:gson:2.9.0")
    }
}
/*tasks.processResources {
    var path = "src/main/java/alix/loaders/bukkit"
    from("$path/plugin.yml") {
        into("/") // Place the file in the root of the JAR
    }
    from("$path/config.yml") {
        into("/") // Place the file in the root of the JAR
    }
}*/

tasks.test {
    useJUnitPlatform()
}