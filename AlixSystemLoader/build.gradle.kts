import sun.jvmstat.monitor.MonitoredVmUtil.jvmArgs

plugins {
    id("java")
    id("java-library")
    id("com.gradleup.shadow") version "9.3.0"
}

group = "AlixSystemLoader"
version = "1.0-SNAPSHOT"
repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io/")

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")

    //kyori
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")

    //packetevents
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    //paper
    maven("https://repo.papermc.io/repository/maven-public/")
    //geyser/floodgate
    maven("https://repo.opencollab.dev/main")
}

tasks.register("prepareKotlinBuildScriptModel") {}

dependencies {
    //later see https://github.com/kyngs/LibreLogin/blob/master/Plugin/build.gradle.kts
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.7")
    implementation("org.postgresql:postgresql:42.7.8")
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("net.kyori:adventure-text-minimessage:4.16.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("net.kyori:adventure-api:4.18.0")
    compileOnly("net.kyori:adventure-nbt:4.18.0")

    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-tree:9.7")
    implementation("net.bytebuddy:byte-buddy-agent:1.14.18")
    implementation("org.roaringbitmap:RoaringBitmap:1.6.9")

    compileOnlyApi("org.geysermc.geyser:api:2.9.0-SNAPSHOT")
    compileOnlyApi("org.geysermc.floodgate:api:2.2.4-SNAPSHOT")

    // https://mvnrepository.com/artifact/com.google.zxing/core
    api("com.google.zxing:core:3.4.0")// https://mvnrepository.com/artifact/com.google.zxing/javase
    api("com.google.zxing:javase:3.4.0")// https://mvnrepository.com/artifact/de.taimos/totp
    api("de.taimos:totp:1.0")// https://mvnrepository.com/artifact/com.nepxion/zxing
    api("com.nepxion:zxing:1.1.1")// https://mvnrepository.com/artifact/com.velocitypowered/velocity-native
    // https://mvnrepository.com/artifact/io.github.bivashy/velocity-native
    compileOnly("io.github.bivashy:velocity-native:3.3.0-410636a")

    compileOnly("com.github.retrooper:packetevents-api:${project.findProperty("packet-events-version")}")
    api(project(":AlixAPI"))

    //compileOnly("org.spigotmc:spigot:1.20.2-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")

    compileOnly("commons-codec:commons-codec:1.16.0")

    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")

    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")

    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    compileOnlyApi("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    compileOnly("io.netty:netty-all:4.1.24.Final")
    compileOnly("com.google.code.gson:gson:2.12.1")
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
} else {
    //cuz they rely on --enable-preview
    tasks.compileJava {
        exclude("alix/common/antibot/epoll/syn/SynJ22Impl.java")
    }
}
tasks.test {
    useJUnitPlatform()
}