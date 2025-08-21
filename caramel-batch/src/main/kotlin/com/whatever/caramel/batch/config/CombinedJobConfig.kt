package com.whatever.caramel.batch.config

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class CombinedJobConfig(
    private val jobRepository: JobRepository,
    private val scheduleRemoveJob: Job,
    private val scheduleAddJob: Job,
) {
    @Bean
    fun combinedJob(
        transactionManager: PlatformTransactionManager,
    ): Job {
        return JobBuilder("combined", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(deleteStep())
            .next(addStep())
            .build()
    }

    @Bean
    fun deleteStep(): Step {
        return StepBuilder("deleteStep", jobRepository)
            .job(scheduleRemoveJob)
            .build()
    }

    @Bean
    fun addStep(): Step {
        return StepBuilder("addStep", jobRepository)
            .job(scheduleAddJob)
            .build()
    }
}
