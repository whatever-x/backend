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
) {

    /**
     * 시간 변경 필요
     */
    @Scheduled(cron = "0 03 23 * * *", zone = "Asia/Seoul")
    fun runJob() {
        val params = JobParametersBuilder()
            .addString("runDate", LocalDate.now().toString())
            .toJobParameters()
        jobLauncher.run(anniversaryJob, params)
    }
    // Job 안에 있는 jobinstance, -> parameter
}
