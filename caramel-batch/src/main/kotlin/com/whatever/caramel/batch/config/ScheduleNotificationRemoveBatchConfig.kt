package com.whatever.caramel.batch.config

import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.domain.notification.service.ScheduledNotificationService
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
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime
import java.time.ZoneId

@Configuration
class ScheduleNotificationRemoveBatchConfig(
    private val scheduledNotificationService: ScheduledNotificationService,
) {
    @Bean
    fun scheduleRemoveItemReader(entityManagerFactory: EntityManagerFactory): ItemReader<ScheduledNotification> {
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
    fun scheduleRemoveItemWriter(): ItemWriter<ScheduledNotification> {
        // JpaItemWriter 도 있어요
        return ItemWriter {
            val targetUserIds = mutableSetOf<Long>()
            val notificationTypes = mutableSetOf<NotificationType>()

            it.items.map { notification ->
                targetUserIds.add(notification.targetUserId)
                notificationTypes.add(notification.notificationType)
            }

            scheduledNotificationService.deleteScheduledNotifications(
                targetUserIds = targetUserIds,
                notificationTypes = notificationTypes,
            )
        }
    }

    @Bean
    fun scheduleRemoveStep(
        whatEverJobRepository: JobRepository,
        @Qualifier("batchTransactionManager") batchTransactionManager: PlatformTransactionManager,
        scheduleRemoveItemReader: ItemReader<ScheduledNotification>,
        scheduleRemoveItemWriter: ItemWriter<ScheduledNotification>,
    ): Step {
        return StepBuilder("delete", whatEverJobRepository)
            .chunk<ScheduledNotification, ScheduledNotification>(10, batchTransactionManager)
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
