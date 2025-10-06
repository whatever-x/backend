import org.springframework.boot.gradle.tasks.bundling.BootJar

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
    tasks.register<org.springframework.boot.gradle.tasks.run.BootRun>("${jobName}Batch") {
        group = "batch"
        mainClass.set("com.whatever.caramel.batch.WhateverBatchApplicationKt")
        classpath = project(":caramel-batch").sourceSets["main"].runtimeClasspath
        args = listOf(
            "--spring.batch.job.name=${jobName}Job",
            "--spring.profiles.active=dev,batch"
        )
    }
}

registerBatchTask("notificationAdd")
registerBatchTask("anniversary")
