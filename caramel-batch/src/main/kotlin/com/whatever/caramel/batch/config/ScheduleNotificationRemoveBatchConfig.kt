package com.whatever.caramel.batch.config

import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.domain.notification.repository.ScheduledNotificationRepository
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDate
import java.time.ZoneId

@Configuration
class ScheduleNotificationRemoveBatchConfig(
    private val scheduledNotificationRepository: ScheduledNotificationRepository,
) {
    @Bean
    @StepScope
    fun scheduleRemoveItemReader(entityManagerFactory: EntityManagerFactory): JpaPagingItemReader<ScheduledNotification> {
        val zoneSource = ZoneId.of("Asia/Seoul")
        val localDate = LocalDate.now(zoneSource)
        val startOfDay = localDate.atStartOfDay(zoneSource).toLocalDateTime()
        val endOfDay = localDate.atTime(23, 59, 59).withNano(0)

        return JpaPagingItemReader<ScheduledNotification>().apply {
            setEntityManagerFactory(entityManagerFactory)
            setQueryString("SELECT s FROM ScheduledNotification s WHERE s.notifyAt BETWEEN :startOfDay AND :endOfDay")
            setParameterValues(mapOf("startOfDay" to startOfDay, "endOfDay" to endOfDay))
            pageSize = 10
            afterPropertiesSet()
        }
    }

    @Bean
    @StepScope
    fun scheduleRemoveItemWriter(): ItemWriter<ScheduledNotification> {
        scheduledNotificationRepository.deleteAllInBatch()
        return ItemWriter {
            /* no-op */
        }
    }

    @Bean
    fun scheduleRemoveStep(
        whatEverJobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        scheduleRemoveItemReader: ItemReader<ScheduledNotification>,
        scheduleRemoveItemWriter: ItemWriter<ScheduledNotification>,
    ): Step {
        return StepBuilder("delete", whatEverJobRepository)
            .chunk<ScheduledNotification, ScheduledNotification>(BatchConfig.DEFAULT_BATCH_SIZE, transactionManager)
            .reader(scheduleRemoveItemReader)
            .writer(scheduleRemoveItemWriter)
            .build()
    }

    @Bean
    fun scheduleRemoveJob(jobRepository: JobRepository, scheduleRemoveStep: Step): Job {
        return JobBuilder("delete", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(scheduleRemoveStep)
            .build()
    }

    @Bean
    fun scheduleRemoveJobExecutionListener(): JobExecutionListener {
        return object : JobExecutionListener {

            override fun afterJob(jobExecution: JobExecution) {
                if (jobExecution.status == BatchStatus.COMPLETED) {
                    println("배치 성공했으니 슬랙 쏘는 것 같은 것도 가능 할지도")
                }
            }
        }
    }
}
