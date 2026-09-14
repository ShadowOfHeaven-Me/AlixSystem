plugins {
    id("java")
    `maven-publish`
    kotlin("jvm")
}

group = "AlixSystem"
version = "${project.findProperty("alix-spigot-version")}"

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "com.github.ShadowOfHeaven-Me"
            artifactId = "AlixSystem"
            version = "${project.findProperty("alix-spigot-version")}"
        }
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

subprojects {
    apply(plugin = "java")

    plugins.withId("java") {
        configure<SourceSetContainer> {
            named("main") {
                java.srcDir("$rootDir/buildSrc/src/main/kotlin")
            }
        }
    }
}

java.toolchain.languageVersion.set(
    JavaLanguageVersion.of(
        Integer.parseInt(
            project.findProperty("toolchain-lang-version").toString()
        )
    )
)