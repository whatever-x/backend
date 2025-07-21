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

    id("jacoco")
}

val springCloudVersion = "2024.0.0"
val opentelemetryVersion = "2.14.0"

subprojects {
    group = "com.whatever"
    repositories {
        mavenCentral()
    }

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
}

dependencies {
    // Spring
//    implementation("org.springframework.boot:spring-boot-starter-web")
//    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//    implementation("org.springframework.boot:spring-boot-starter-data-redis")
//    implementation("org.springframework.boot:spring-boot-starter-validation")
//    implementation("org.springframework.boot:spring-boot-starter-security")
//    implementation("org.springframework.boot:spring-boot-starter-aop")
//    implementation("org.springframework.retry:spring-retry")
//    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:3.5.3")
//    implementation("com.linecorp.kotlin-jdsl:jpql-render:3.5.3")
//    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:3.5.3")

    // OpenFeign
//    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
//    implementation("io.github.openfeign:feign-okhttp:13.5")

    // Cache
//    implementation("org.springframework.boot:spring-boot-starter-cache")
//    implementation("com.github.ben-manes.caffeine:caffeine")

    // Logging
//    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
//    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
//    implementation("org.springframework.boot:spring-boot-starter-log4j2")
//    implementation("io.opentelemetry.instrumentation:opentelemetry-log4j-appender-2.17:$opentelemetryVersion-alpha")

    // Swagger
//    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.3")

    // Metric
//    implementation("org.springframework.boot:spring-boot-starter-actuator")
//    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // JWT
//    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
//    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
//    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Firebase Admin SDK
//    implementation("com.google.firebase:firebase-admin:9.4.3")

    // DB
//    runtimeOnly("org.postgresql:postgresql")

    // Util
//    implementation("io.viascom.nanoid:nanoid:1.0.1")

    // test
//    runtimeOnly("com.h2database:h2")
//    testImplementation("org.springframework.security:spring-security-test")

//    testImplementation("org.awaitility:awaitility-kotlin:4.3.0")
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}
