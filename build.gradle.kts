plugins {
    id("java")
    `maven-publish`
}

group = "AlixSystem"
version = "3.6.1"

repositories {
    mavenCentral()
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

dependencies {
}
java.toolchain.languageVersion = JavaLanguageVersion.of(21)