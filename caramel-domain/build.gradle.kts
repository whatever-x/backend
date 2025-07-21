import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.springframework.boot.gradle.tasks.bundling.BootJar

val springCloudVersion = "2024.0.0"
val opentelemetryVersion = "2.14.0"

group = "com.whatever.carmel-domain"
version = "0.0.1-SNAPSHOT"

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
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

    // Cache
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")

    // DB
    runtimeOnly("org.postgresql:postgresql")
    // Util
    implementation("io.viascom.nanoid:nanoid:1.0.1")
    // test
    runtimeOnly("com.h2database:h2")
    testImplementation(kotlin("test"))  // ????????
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

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:$opentelemetryVersion")
    }
}
tasks.getByName<BootJar>("bootJar") {
    enabled = false
}
tasks.getByName<Jar>("jar") {
    enabled = true
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
