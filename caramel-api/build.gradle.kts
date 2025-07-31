import org.springframework.boot.gradle.tasks.bundling.BootJar

group = "com.whatever.caramel-api"
version = "0.0.1-SNAPSHOT"

val springCloudVersion = "2024.0.0"
val opentelemetryVersion = "2.14.0"

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
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
    implementation("io.opentelemetry.instrumentation:opentelemetry-log4j-appender-2.17:$opentelemetryVersion-alpha")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.3")

    // Metric
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // test
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.getByName<BootJar>("bootJar") {
    enabled = true
}
tasks.getByName<Jar>("jar") {
    enabled = true
}
tasks.withType<Test> {
    workingDir = rootDir
}