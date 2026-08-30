package com.whatever.caramel.batch.config.job

import com.whatever.caramel.batch.config.listener.AnniversaryJobListener
import com.whatever.caramel.batch.config.listener.AnniversaryStepListener
import com.whatever.caramel.common.util.DateTimeUtil
import com.whatever.caramel.common.util.DateTimeUtil.KST_ZONE_ID
import com.whatever.caramel.domain.firebase.service.FirebaseService
import com.whatever.caramel.domain.notification.model.NotificationHistory
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.domain.notification.repository.NotificationHistoryRepository
import com.whatever.caramel.domain.notification.repository.ScheduledNotificationRepository
import com.whatever.caramel.infrastructure.firebase.model.FcmNotification
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDate

@Configuration
class AnniversaryFcmBatchConfig(
    private val whatEverJobRepository: JobRepository,
    private val apiEntityManagerFactory: EntityManagerFactory,
    private val firebaseService: FirebaseService,
    private val notificationHistoryRepository: NotificationHistoryRepository,
) {
    @Bean
    @StepScope
    fun anniversaryItemReader(
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @Value("#{jobParameters['runDate']}") runDate: LocalDate,
    ): JpaPagingItemReader<ScheduledNotification> {
        val startOfDay = runDate.atStartOfDay()
        val endOfDay = startOfDay.plusDays(1).withNano(0)

        return JpaPagingItemReaderBuilder<ScheduledNotification>()
            .name("anniversaryItemReader")
            .entityManagerFactory(apiEntityManagerFactory)
            .queryString(
                """
                    SELECT s FROM ScheduledNotification s 
                    WHERE s.notifyAt >= :startOfDay 
                    AND s.notifyAt < :endOfDay
                    ORDER BY s.id
                    """.trimIndent()
            )
            .parameterValues(
                mapOf(
                    "startOfDay" to startOfDay,
                    "endOfDay" to endOfDay,
                )
            )
            .pageSize(FCM_PAGE_SIZE)
            .transacted(false)
            .build()
    }

    @Bean
    fun anniversaryItemWriter(): ItemWriter<ScheduledNotification> {
        return ItemWriter { chunk ->
            chunk.items.forEach { notification ->
                with(notification) {
                    runCatching {
                        firebaseService.sendNotification(
                            targetUserIds = setOf(notification.targetUserId),
                            fcmNotification = FcmNotification(title = title, body = body, image = image),
                        )
                    }.onFailure { exception ->
                        firebaseService.removeUnregisteredTokens(exception)
                        notificationHistoryRepository.save(
                            NotificationHistory.failed(
                                source = notification,
                                errorMessage = exception.message.orEmpty(),
                            )
                        )
                    }.onSuccess {
                        notificationHistoryRepository.save(
                            NotificationHistory.succeeded(
                                source = notification,
                                sentAt = DateTimeUtil.localNow(zoneId = KST_ZONE_ID),
                            )
                        )
                    }
                }
            }
        }
    }

    @Bean
    fun anniversaryStep(
        transactionManager: PlatformTransactionManager,
        anniversaryItemReader: ItemReader<ScheduledNotification>,
        anniversaryItemWriter: ItemWriter<ScheduledNotification>,
        anniversaryStepListener: AnniversaryStepListener,
    ): Step {
        return StepBuilder("anniversaryStep", whatEverJobRepository)
            .chunk<ScheduledNotification, ScheduledNotification>(FCM_CHUNK_SIZE, transactionManager)
            .reader(anniversaryItemReader)
            .writer(anniversaryItemWriter)
            .listener(anniversaryStepListener)
            .build()
    }

    @Bean
    fun removeStep(
        transactionManager: PlatformTransactionManager,
        scheduledNotificationRepository: ScheduledNotificationRepository,
    ): Step {
        return StepBuilder("removeStep", whatEverJobRepository)
            .tasklet({ _, _ ->
                scheduledNotificationRepository.deleteAllInBatch()
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }

    @Bean
    fun anniversaryJob(
        jobRepository: JobRepository,
        anniversaryStep: Step,
        removeStep: Step,
        anniversaryJobListener: AnniversaryJobListener,
    ): Job {
        return JobBuilder("anniversaryJob", jobRepository)
            .start(anniversaryStep)
            .next(removeStep)
            .listener(anniversaryJobListener)
            .build()
    }

    companion object {
        private const val FCM_PAGE_SIZE = 10
        private const val FCM_CHUNK_SIZE = 10
    }
}
