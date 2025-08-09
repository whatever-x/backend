package com.whatever.caramel.batch.config

import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.domain.notification.service.ScheduledNotificationService
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
import org.springframework.batch.item.support.ListItemReader
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
    // 겨우 이거떄문에 의존성 가져가야하는가?
    @Bean
    fun scheduleRemoveItemReader(): ItemReader<ScheduledNotification> {
        val date: LocalDateTime = DateTimeUtil.localNow(FcmBatchConfig.TARGET_ZONE_ID)
        val scheduleList = scheduledNotificationService.getMatchedScheduledNotifications(date)
        return ListItemReader(scheduleList)
    }

    @Bean
    fun scheduleRemoveItemWriter(): ItemWriter<ScheduledNotification> {
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
            .allowStartIfComplete(true)
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

    companion object {
        private val TARGET_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
