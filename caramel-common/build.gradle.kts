import org.springframework.boot.gradle.tasks.bundling.BootJar

group = "com.whatever.caramel-common"
version = "0.0.1-SNAPSHOT"

dependencies {
    // Spring
    implementation("org.springframework:spring-web")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}
tasks.getByName<Jar>("jar") {
    enabled = true
}
