import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"

    // Kotlin 플러그인
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    kotlin("plugin.jpa") version "2.1.20"
}

group = "com.whatever.caramel-infrastructure"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

val springCloudVersion = "2024.0.0"
val opentelemetryVersion = "2.14.0"

dependencies {
    implementation(project(":caramel-common"))

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")

    // Jackson과 Kotlin의 호환성을 위한 모듈
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // OpenFeign
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("io.github.openfeign:feign-okhttp:13.5")

    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.4.3")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:$opentelemetryVersion")
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}
tasks.getByName<Jar>("jar") {
    enabled = true
}
