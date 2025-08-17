package com.whatever.caramel.batch

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class AnniversaryBatchScheduler(
    private val jobLauncher: JobLauncher,
    private val anniversaryJob: Job,
    private val combinedJob: Job,
) {

    @Scheduled(cron = "0 40 23 * * *", zone = "Asia/Seoul")
    fun runAnniversaryJob() {
        val params = JobParametersBuilder()
            .addString("anniversary", LocalDate.now().toString())
            .toJobParameters()
        jobLauncher.run(anniversaryJob, params)
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    fun runCombinedJob() {
        val params = JobParametersBuilder()
            .addString("combinedJob", LocalDate.now().toString())
            .toJobParameters()
        jobLauncher.run(combinedJob, params)
    }
}
