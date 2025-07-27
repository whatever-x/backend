import org.springframework.boot.gradle.tasks.bundling.BootJar

group = "com.whatever.caramel-infrastructure"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":caramel-common"))

    // Spring
    implementation("org.springframework:spring-web")
    implementation("org.springframework.data:spring-data-commons")

    // OpenFeign
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("io.github.openfeign:feign-okhttp:13.5")

    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.4.3")
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}
tasks.getByName<Jar>("jar") {
    enabled = true
}
