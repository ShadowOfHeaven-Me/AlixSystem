import org.gradle.kotlin.dsl.implementation

plugins {//java-library
    id("java")
    id("java-library")
    id("com.gradleup.shadow") version "9.6.1"
    id("de.eldoria.plugin-yml.bukkit") version "0.8.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
    id("xyz.kyngs.mcupload.plugin").version("0.3.4")
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

bukkit {
    name = "AlixSystem"
    version = project.findProperty("alix-spigot-version") as String
    main = "alix.loaders.bukkit.BukkitAlixMain"
    author = "ShadowOfHeaven"
    apiVersion = "1.13"
    softDepend = listOf(
        "SkinsRestorer", "FastLogin", "LuckPerms", "floodgate",
        "Essentials"//to ensure it loads first, and has it's commands registered first, so that we can potentially override them
    )
}


mcupload {
    file = tasks.shadowJar
    swallowErrors = true

    platforms {
        modrinth {
            loaders = listOf("paper", "purpur", "spigot", "bukkit")
            projectId = "dXkFpOAK"
            gameVersions = listOf(
                "26.3", "26.2", "26.1.2","26.1.1","26.1",
                "1.21.11", "1.21.10", "1.21.9", "1.21.8", "1.21.7", "1.21.6", "1.21.5", "1.21.4", "1.21.3", "1.21.2", "1.21.1", "1.21",
                "1.20.6", "1.20.5", "1.20.4", "1.20.3", "1.20.2", "1.20.1", "1.20",
                "1.19.4", "1.19.3", "1.19.2", "1.19.1", "1.19",
                "1.18.2", "1.18.1", "1.18",
                "1.17.1", "1.17",
                "1.16.5", "1.16.4", "1.16.3", "1.16.2", "1.16.1", "1.16",
                "1.15.2", "1.15.1", "1.15",
                "1.14.4", "1.14.3", "1.14.2", "1.14.1", "1.14",
                "1.13.2", "1.13.1", "1.13",
                "1.12.2", "1.12.1", "1.12"
            )
            token = System.getenv("MODRINTH_TOKEN")
        }
    }
    datasource {
        file {
            readmeFile = "README.md"
            changelogFile = "CHANGELOG.md"
        }
    }
}
tasks.named("release") {
    notCompatibleWithConfigurationCache("mcupload plugin does not support configuration cache yet")
}
tasks.shadowJar {
    destinationDirectory = file(project.findProperty("build-dir") as String)
    archiveBaseName.set("AlixSystem")
    archiveClassifier.set("")//w pizde z z tym "-all" suffixem
    //Kept as the task-level DEFAULT only for the sake of mergeServiceFiles() below: without
    //DuplicatesStrategy.INCLUDE, Shadow's own duplicate-filtering drops every duplicate copy of a path
    //before the ServiceFileTransformer that mergeServiceFiles() installs ever gets to see it - confirmed by
    //actually building both ways and inspecting META-INF/services/java.sql.Driver in the output jar: with
    //EXCLUDE, only mariadb-java-client's driver entry survived and postgresql's was silently lost, breaking
    //DriverManager's automatic PostgreSQL support even though the driver's classes were still shaded in.
    //INCLUDE is overridden back to the default (EXCLUDE, "first one wins") for every OTHER path below via
    //filesMatching(), since applying it jar-wide has its own bug: packetevents-spigot is shaded in further
    //down (relocated, so its CLASSES don't collide with the standalone packetevents plugin players also
    //run), but its own bundled plugin.yml (declaring name: packetevents) is NOT relocated - it lands at the
    //exact same "plugin.yml" path as this plugin's own generated one. Under a jar-wide INCLUDE, both copies
    //were written into the final jar back-to-back, and Paper's ModernPluginLoadingStrategy read that as
    //this jar ALSO claiming to be "packetevents", logging "Ambiguous plugin name 'packetevents'" against
    //the real standalone packetevents jar - also confirmed by building and inspecting the jar directly.
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    val prefix = "alix.libs"
    var list = listOf(
        "io.github.retrooper.packetevents",
        "com.github.retrooper.packetevents",
        "net.kyori.adventure.api",
        "net.kyori.adventure.nbt",
        //Unrelocated JDBC/pool classes (org.mariadb.jdbc, org.postgresql, com.zaxxer.hikari) previously
        //triggered LibertyBans' (and any other plugin bundling the same libraries) unrelocated-library
        //stability warning, since two plugins' classloaders each carrying their own unrelocated copy of the
        //same class can collide through java.sql.DriverManager's JVM-wide (not per-classloader) driver
        //registry. Relocating them was previously abandoned because it silently dropped PostgreSQL's
        //META-INF/services/java.sql.Driver entry (see the mergeServiceFiles()/duplicatesStrategy comment
        //above) - now that eachFile below narrows duplicatesStrategy back to EXCLUDE everywhere except
        //META-INF/services/*, that merge bug no longer applies, so these can be relocated too.
        "org.mariadb.jdbc",
        "org.postgresql",
        "com.zaxxer.hikari"
    )

    for (s in list) {
        relocate(s, "$prefix.$s")
    }
    mergeServiceFiles()

    //Narrows the task-level INCLUDE (see the comment on duplicatesStrategy above) back down to ONLY
    //META-INF/services/* - every other duplicate path (plugin.yml being the concrete case that matters)
    //falls back to EXCLUDE ("first one wins") instead.
    eachFile {
        if (!this.path.startsWith("META-INF/services/"))
            this.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    /*relocate("io.github.retrooper.packetevents", "$prefix.io.github.retrooper.packetevents")
    relocate("com.github.retrooper.packetevents", "$prefix.com.github.retrooper.packetevents")
    relocate("net.kyori:adventure-api", "$prefix.net.kyori:adventure-api")
    relocate("net.kyori:adventure-nbt", "$prefix.net.kyori:adventure-nbt")*/


    minimize {
        exclude(dependency("org.mariadb.jdbc:mariadb-java-client:.*"))
        exclude(dependency("org.postgresql:postgresql:.*"))
    }
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
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    //geyser/floodgate
    maven("https://repo.opencollab.dev/main")
}
//się wypierdala czasami bez tego
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

    compileOnlyApi("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")

    implementation("io.papermc:paperlib:1.0.7")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    //Newer Gradle no longer bundles this implicitly - without it, the test task can't even start the JUnit
    //Platform test executor ("Failed to load JUnit Platform... including the JUnit Platform launcher"),
    //regardless of whether there's anything to actually run (src/test/java here only holds SpigotMessagesMaker,
    //a standalone dev script with main(), not a real JUnit test - see failOnNoDiscoveredTests below).
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")

    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
}

configurations.all {
    resolutionStrategy {
        force("com.google.code.gson:gson:2.9.0")
    }
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

java.toolchain.languageVersion.set(JavaLanguageVersion.of(Integer.parseInt(project.findProperty("toolchain-lang-version").toString())))

//AlixAPISpigot's shadowJar is configured (archiveClassifier = "") to overwrite its own plain jar's output
//file - that's the artifact this project actually needs on its compile classpath, but Gradle only knows to
//wait for AlixAPISpigot's plain ":jar" task (the project dependency's default outgoing artifact), not its
//shadowJar. Without this, Gradle 9's stricter validation flags it as an undeclared implicit dependency
//("uses this output of task ':AlixAPI:AlixAPISpigot:shadowJar' without declaring..."), since nothing here
//tells it that compileJava must wait for shadowJar to finish rewriting that file first.
tasks.compileJava {
    dependsOn(":AlixAPI:AlixAPISpigot:shadowJar")
}
tasks.test {
    useJUnitPlatform()
    failOnNoDiscoveredTests = false
}