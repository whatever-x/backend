package com.whatever.caramel.batch.config

import com.whatever.caramel.batch.entity.BatchFcmNotification
import com.whatever.caramel.domain.firebase.service.FirebaseService
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.domain.notification.service.ScheduledNotificationService
import com.whatever.caramel.infrastructure.firebase.model.FcmNotification
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.ListItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDate
import java.time.ZoneId

@Configuration
class FcmBatchConfig(
    private val scheduledNotificationService: ScheduledNotificationService,
    private val firebaseService: FirebaseService,
) {
    @Bean
    @StepScope
    fun anniversaryItemReader(): ItemReader<ScheduledNotification> {
        val zoneSource = ZoneId.of("Asia/Seoul")
        val localDate = LocalDate.now(zoneSource)
        val startOfDay = localDate.atStartOfDay(zoneSource).toLocalDateTime()
        val endOfDay = localDate.atTime(23, 59, 59).withNano(0)
        val scheduledNotificationList =
            scheduledNotificationService.getMatchedScheduledNotifications(startOfDay, endOfDay)

        return ListItemReader(scheduledNotificationList)
    }

    @Bean
    fun compositeItemProcessor(): ItemProcessor<ScheduledNotification, BatchFcmNotification> {
        return ItemProcessor<ScheduledNotification, BatchFcmNotification> { notification ->
            val fcmNotification = FcmNotification(
                title = notification.title,
                body = notification.body,
                image = notification.image,
            )
            BatchFcmNotification(
                targetId = notification.targetUserId,
                fcmNotification = fcmNotification,
            )
        }
    }

    @Bean
    fun anniversaryItemWriter(): ItemWriter<BatchFcmNotification> {
        return ItemWriter { chunk ->
            chunk.items.map { notification ->
                with(notification) {
                    firebaseService.sendNotification(
                        setOf(targetId),
                        FcmNotification(
                            title = fcmNotification.title,
                            body = fcmNotification.body,
                            image = fcmNotification.image,
                        )
                    )
                }
            }
        }
    }

    @Bean
    fun anniversaryStep(
        whatEverJobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        anniversaryItemReader: ItemReader<ScheduledNotification>,
        compositeItemProcessor: ItemProcessor<ScheduledNotification, BatchFcmNotification>,
        anniversaryItemWriter: ItemWriter<BatchFcmNotification>,
    ): Step {
        return StepBuilder("anniversary", whatEverJobRepository)
            .chunk<ScheduledNotification, BatchFcmNotification>(BatchConfig.DEFAULT_BATCH_SIZE, transactionManager)
            .reader(anniversaryItemReader)
            .processor(compositeItemProcessor)
            .writer(anniversaryItemWriter)
            .build()
    }

    @Bean
    fun anniversaryJob(jobRepository: JobRepository, anniversaryStep: Step): Job {
        return JobBuilder("anniversary", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(anniversaryStep)
            .build()
    }
}
