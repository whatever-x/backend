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
    private val scheduleRemoveJob: Job,
) {

    /**
     * 협의해서 시간 변경 필요
     */
    @Scheduled(cron = "0 50 23 * * *", zone = "Asia/Seoul")
    fun runAnniversaryJob() {
        val params = JobParametersBuilder()
            .addString("anniversary", LocalDate.now().toString())
            .toJobParameters()
        jobLauncher.run(anniversaryJob, params)
    }

    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    fun runDeleteJob() {
        val params = JobParametersBuilder()
            .addString("delete", LocalDate.now().toString())
            .toJobParameters()
        jobLauncher.run(scheduleRemoveJob, params)
    }
}
