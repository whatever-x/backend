package com.whatever.caramel.batch.config

import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.domain.firebase.service.FirebaseService
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.domain.notification.service.ScheduledNotificationService
import com.whatever.caramel.infrastructure.firebase.model.FcmNotification
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
import org.springframework.batch.item.support.ListItemReader
import org.springframework.boot.autoconfigure.batch.BatchTransactionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime
import java.time.ZoneId

@Configuration
class FcmBatchConfig(
    private val scheduledNotificationService: ScheduledNotificationService,
    private val firebaseService: FirebaseService,
) {
    // 겨우 이거떄문에 의존성 가져가야하는가?
    @Bean
    fun anniversaryItemReader(): ItemReader<ScheduledNotification> {
        val date: LocalDateTime = DateTimeUtil.localNow(TARGET_ZONE_ID)
        val scheduleList = scheduledNotificationService.getMatchedScheduledNotifications(date)
        return ListItemReader(scheduleList)
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

    companion object {
        internal val TARGET_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
    }
}
