plugins {
    kotlin("jvm") version "1.9.0"
    id("io.ktor.plugin") version "2.3.0"
    kotlin("plugin.serialization") version "1.9.10"
}

group = "com.mehmet"
version = "0.0.1"

application {
    mainClass.set("com.mehmet.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.3.0")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.0")
    implementation("io.ktor:ktor-server-auth-jvm:2.3.0")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:2.3.0")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.0")
    implementation("io.ktor:ktor-serialization-gson-jvm:2.3.0")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.0")
    implementation("io.ktor:ktor-client-core-jvm:2.3.0")
    implementation("io.ktor:ktor-client-cio-jvm:2.3.0")
    implementation("org.litote.kmongo:kmongo:4.7.1")
    implementation("org.litote.kmongo:kmongo-coroutine:4.7.1")
    implementation("io.ktor:ktor-client-serialization-jvm:2.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.1")
    implementation("com.github.luben:zstd-jni:1.5.2-5")
    implementation("com.google.code.gson:gson:2.9.0")
    testImplementation("io.ktor:ktor-server-tests-jvm:2.3.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.8.10")
    implementation("org.yaml:snakeyaml:2.0")
    implementation("com.google.guava:guava:32.0.1-jre")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("ch.qos.logback:logback-classic:1.4.11")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))  // Java 17 kullanılıyor
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17" // JVM hedef sürümünüz
    }
}

