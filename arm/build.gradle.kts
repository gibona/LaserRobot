import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    java
    application
}


group = "me.gibona"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
    google()
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

dependencies {
    implementation("org.testng:testng:7.1.0")
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    //implementation("org.jetbrains.kotlin:kotlin-runtime")
    //implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}