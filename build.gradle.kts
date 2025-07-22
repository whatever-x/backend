import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

/** Root Gradle **/

plugins {
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"

    // Kotlin 플러그인
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    kotlin("plugin.jpa") version "2.1.20"
}

val springCloudVersion = "2024.0.0"
val opentelemetryVersion = "2.14.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = "com.whatever"

    // 하위 모듈에 공통 플러그인 적용
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "java-library")
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-jpa")
    apply(plugin = "kotlin-spring")
    apply(plugin = "kotlin-kapt")
    apply(plugin = "jacoco")

    // 공통 Java & Kotlin 버전 세팅
    java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    dependencies {
        // Logging
        implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
        implementation("org.springframework.boot:spring-boot-starter-log4j2")

        // Jackson과 Kotlin의 호환성을 위한 모듈
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        // 리플렉션을 지원하는 Kotlin 라이브러리
        implementation("org.jetbrains.kotlin:kotlin-reflect")

        // Test
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
        testImplementation("io.mockk:mockk:1.14.4")
    }

    // starter-logging의 기본 logback 제외
    configurations.configureEach {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
            mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:$opentelemetryVersion")
        }
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}
