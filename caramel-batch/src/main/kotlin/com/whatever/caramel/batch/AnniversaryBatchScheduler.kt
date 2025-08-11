package com.whatever.caramel.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class AnniversaryBatchScheduler(
    private val jobLauncher: JobLauncher,
    private val anniversaryJob: Job,
    private val scheduleRemoveJob: Job,
) {

    /**
     * 협의해서 시간 변경 필요
     */
    @Scheduled(cron = "0 44 3 * * *", zone = "Asia/Seoul")
    fun runAnniversaryJob() {
        val params = JobParametersBuilder()
            .addString("anniversary", System.currentTimeMillis().toString())
            .toJobParameters()
        println("tjrwn run job 수행")
        jobLauncher.run(anniversaryJob, params)
    }

    @Scheduled(cron = "0 46 3 * * *", zone = "Asia/Seoul")
    fun runDeleteJob() {
        val params = JobParametersBuilder()
            .addString("delete", System.currentTimeMillis().toString())
            .toJobParameters()
        println("tjrwn delete job 수행")
        jobLauncher.run(scheduleRemoveJob, params)
    }
}
