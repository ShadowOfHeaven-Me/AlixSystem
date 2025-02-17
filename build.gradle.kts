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
        create<MavenPublication>("release") {
            from(components["java"])
            groupId = "com.github.ShadowOfHeaven-Me"
            artifactId = "AlixSystem"
            version = project.version as String
        }
    }
}

dependencies {
}