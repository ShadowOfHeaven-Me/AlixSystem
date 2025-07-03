plugins {
    id("java")
    `maven-publish`
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
}
java.toolchain.languageVersion = JavaLanguageVersion.of(21)