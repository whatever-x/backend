import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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

group = "com.whatever.carmel-domain"
version = "0.0.1-SNAPSHOT"

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":caramel-common"))
    implementation(project(":caramel-infrastructure"))
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:3.5.3")
    implementation("com.linecorp.kotlin-jdsl:jpql-render:3.5.3")
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:3.5.3")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-aop")  // dddd
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")

    // Cache
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    // runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    // runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Jackson과 Kotlin의 호환성을 위한 모듈
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // 리플렉션을 지원하는 Kotlin 라이브러리
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // DB
    runtimeOnly("org.postgresql:postgresql")
    // Util
    implementation("io.viascom.nanoid:nanoid:1.0.1")
    // test
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.awaitility:awaitility-kotlin:4.3.0")
    // testImplementation("com.github.codemonstur:embedded-redis:1.4.3")
    testImplementation("io.mockk:mockk:1.14.4")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

configurations.configureEach {
    // 기본 로깅 스타터 제외
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    finalizedBy(tasks.named("jacocoTestReport")) // 테스트 실행 후 jacoco 리포트 생성
}
jacoco {
    toolVersion = "0.8.11"
    reportsDirectory.set(layout.buildDirectory.dir("jacocoXml"))
}
tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named<Test>("test")) // test 태스크에 의존
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
    // 분석할 클래스 파일 필터링 (Kotlin DSL에 더 적합한 방식으로 수정)
    classDirectories.setFrom(
        files(
            classDirectories.asFileTree.matching {
                include("**/domain/**/service/*Service.class")
                exclude(
                    "**/dto/**",
                    "**/controller/**",
                    "**/config/**",
                    "**/global/**"
                )
            }
        )
    )
}
