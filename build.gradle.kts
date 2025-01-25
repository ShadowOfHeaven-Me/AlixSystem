plugins {
    id("java")
    `maven-publish`
    signing
    id ("com.gradleup.shadow") version "8.3.2"
}

group = "AlixSystem"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
}