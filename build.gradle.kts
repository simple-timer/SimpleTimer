import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"

    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://m2.dv8tion.net/releases")
}

val exposedVersion = "0.50.0"

dependencies {
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.7.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    //Discord連携 JDA
    implementation("net.dv8tion", "JDA", "5.0.0-beta.20")
    //JDAに必要なもの
    implementation("ch.qos.logback", "logback-classic", "1.4.14")
    //音再生
    implementation("com.github.walkyst", "lavaplayer-fork", "1.4.3")
    implementation("com.github.Walkyst", "lavaplayer-natives-fork", "1.0.2")

    //YAML
    implementation("com.charleskorn.kaml", "kaml", "0.57.0")
    //Json
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.6.2")

    //BCDice
    implementation("com.github.simple-timer", "bcdice-kt", "1.7.2")

    //RESTApi
    implementation("com.github.kittinunf.fuel", "fuel", "2.3.1")

    //SQL
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
    implementation("org.postgresql:postgresql:42.7.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
}

val jar by tasks.getting(Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "dev.simpletimer.SimpleTimerKt"
    }

    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })

    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}