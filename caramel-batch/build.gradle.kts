import org.springframework.boot.gradle.tasks.bundling.BootJar
import java.time.LocalDate
import java.time.ZoneId

group = "com.whatever.caramel-batch"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":caramel-domain"))
    implementation(project(":caramel-common"))
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
    enabled = true
}
tasks.getByName<Jar>("jar") {
    enabled = true
}

fun registerBatchTask(jobName: String) {
    val zoneSource = ZoneId.of("Asia/Seoul")
    val localDate = LocalDate.now(zoneSource)

    tasks.register<org.springframework.boot.gradle.tasks.run.BootRun>("${jobName}Batch") {
        group = "batch"
        mainClass.set("com.whatever.caramel.batch.WhateverBatchApplicationKt")
        classpath = project(":caramel-batch").sourceSets["main"].runtimeClasspath
        args = listOf(
            "--spring.profiles.active=dev,batch",
            "--spring.batch.job.name=${jobName}Job",
            "runDate=$localDate",
        )
    }
}

registerBatchTask("notificationAdd")
registerBatchTask("anniversary")
