plugins {
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    // Kotlin 플러그인 (Helper 함수 사용)
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    id("jacoco")
}

val springCloudVersion = "2024.0.0"
val opentelemetryVersion = "2.14.0"

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
group = "com.whatever.caramel-api"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
repositories {
    mavenCentral()
}
dependencies {
    implementation(project(":caramel-domain"))
    implementation(project(":caramel-common"))
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.retry:spring-retry")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("io.opentelemetry.instrumentation:opentelemetry-log4j-appender-2.17:$opentelemetryVersion-alpha")
    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.3")
    // Metric
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Jackson과 Kotlin의 호환성을 위한 모듈
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // 리플렉션을 지원하는 Kotlin 라이브러리
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // test
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("io.mockk:mockk:1.14.4")
}
configurations.configureEach {
    // 기본 로깅 스타터 제외
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:$opentelemetryVersion")
    }
}
