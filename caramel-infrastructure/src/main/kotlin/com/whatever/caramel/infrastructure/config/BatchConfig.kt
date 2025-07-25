package com.whatever.caramel.infrastructure.config

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.support.JdbcTransactionManager
import javax.sql.DataSource

@Configuration
class BatchConfig(
    private val dataSource: DataSource,
) {
    @Bean
    fun transactionManager(): JdbcTransactionManager {
        return JdbcTransactionManager(dataSource)
    }

    @Bean
    fun step(jobRepository: JobRepository, transactionManager: JdbcTransactionManager): Step {
        return StepBuilder("step", jobRepository)
            .tasklet(
                { contribution, chunkContext ->
                    println("Hello tjrwn Batch World")
                    RepeatStatus.FINISHED
                }, transactionManager
            )
            .allowStartIfComplete(true)
            .build()
    }

    @Bean
    fun job(jobRepository: JobRepository, step: Step): Job {
        return JobBuilder("job", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(step)
            .build()
    }
}
