import org.springframework.boot.gradle.tasks.bundling.BootJar

group = "com.whatever.caramel-batch"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":caramel-domain"))
    implementation(project(":caramel-infrastructure"))

    // Spring Batch
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}

/**
 * 추후 테스트 코드를 위해 미리 생성
 */
tasks.test {
    useJUnitPlatform()
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}
tasks.getByName<Jar>("jar") {
    enabled = true
}
