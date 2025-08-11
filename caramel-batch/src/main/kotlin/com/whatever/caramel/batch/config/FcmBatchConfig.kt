package com.whatever.caramel.batch.config

import com.whatever.caramel.domain.firebase.service.FirebaseService
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.infrastructure.firebase.model.FcmNotification
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.boot.autoconfigure.batch.BatchTransactionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime
import java.time.ZoneId

@Configuration
class FcmBatchConfig(
    private val firebaseService: FirebaseService,
) {
    @Bean
    fun anniversaryItemReader(entityManagerFactory: EntityManagerFactory): ItemReader<ScheduledNotification> {
        val zoneSource = ZoneId.of("Asia/Seoul")
        val localDateTime = LocalDateTime.now(zoneSource)
        val startOfDay = localDateTime.toLocalDate().atStartOfDay(zoneSource).toLocalDateTime()
        val endOfDay = localDateTime.toLocalDate().atTime(23, 59, 59)

        return JpaPagingItemReader<ScheduledNotification>().apply {
            setEntityManagerFactory(entityManagerFactory)
            setQueryString("SELECT s FROM ScheduledNotification s WHERE s.notifyAt BETWEEN :startOfDay AND :endOfDay")
            setParameterValues(mapOf("startOfDay" to startOfDay, "endOfDay" to endOfDay))
            pageSize = 10
            afterPropertiesSet()
        }
    }

    @Bean
    fun compositeItemProcessor(): ItemProcessor<ScheduledNotification, ScheduledNotification> {
        return ItemProcessor<ScheduledNotification, ScheduledNotification> {
            val fcmNotification = FcmNotification(
                title = it.title,
                body = it.body,
            )
            firebaseService.sendNotification(
                setOf(it.targetUserId),
                fcmNotification
            )
            it
        }
    }

    @Bean
    fun anniversaryItemWriter(): ItemWriter<ScheduledNotification> {
        return ItemWriter { /*no-op*/ }
    }

    @Bean
    fun anniversaryStep(
        whatEverJobRepository: JobRepository,
        @BatchTransactionManager batchTransactionManager: PlatformTransactionManager,
        anniversaryItemReader: ItemReader<ScheduledNotification>,
        compositeItemProcessor: ItemProcessor<ScheduledNotification, ScheduledNotification>,
        anniversaryItemWriter: ItemWriter<ScheduledNotification>,
    ): Step {
        return StepBuilder("anniversary", whatEverJobRepository)
            .chunk<ScheduledNotification, ScheduledNotification>(10, batchTransactionManager)
            .reader(anniversaryItemReader)
            .processor(compositeItemProcessor)
            .writer(anniversaryItemWriter)
            .allowStartIfComplete(true)
            .build()
    }

    @Bean
    fun anniversaryJob(jobRepository: JobRepository, anniversaryStep: Step): Job {
        return JobBuilder("anniversary", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(anniversaryStep)
            .build()
    }

    @Bean
    fun jobExecutionListener(): JobExecutionListener {
        return object : JobExecutionListener {

            override fun afterJob(jobExecution: JobExecution) {
                if (jobExecution.status == BatchStatus.COMPLETED) {
                    println(" 배치 성공했으니 디비 전부 제거같은 것 수행도 가능")
                }
            }
        }
    }
}
