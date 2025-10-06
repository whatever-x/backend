package com.whatever.caramel.batch.config.job

import com.whatever.caramel.batch.entity.BatchFcmNotification
import com.whatever.caramel.domain.firebase.service.FirebaseService
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.domain.notification.repository.ScheduledNotificationRepository
import com.whatever.caramel.infrastructure.firebase.model.FcmNotification
import jakarta.persistence.EntityManagerFactory
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
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDate
import java.time.ZoneId

@Configuration
class AnniversaryBatchConfig(
    private val whatEverJobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val entityManagerFactory: EntityManagerFactory,
) {
    @Bean
    @StepScope
    fun anniversaryItemReader(): JpaPagingItemReader<ScheduledNotification> {
        val zoneSource = ZoneId.of("Asia/Seoul")
        val localDate = LocalDate.now(zoneSource)
        val startOfDay = localDate.atStartOfDay(zoneSource).toLocalDateTime()
        val endOfDay = localDate.atTime(23, 59, 59).withNano(0)

        return JpaPagingItemReaderBuilder<ScheduledNotification>()
            .name("anniversaryItemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString(
                """
                    SELECT s FROM ScheduledNotification s 
                    WHERE s.notifyAt BETWEEN :startOfDay AND :endOfDay
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
            .build()
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
    fun anniversaryItemWriter(
        firebaseService: FirebaseService,
    ): ItemWriter<BatchFcmNotification> {
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
        anniversaryItemReader: ItemReader<ScheduledNotification>,
        compositeItemProcessor: ItemProcessor<ScheduledNotification, BatchFcmNotification>,
        anniversaryItemWriter: ItemWriter<BatchFcmNotification>,
    ): Step {
        return StepBuilder("anniversaryStep", whatEverJobRepository)
            .chunk<ScheduledNotification, BatchFcmNotification>(FCM_CHUNK_SIZE, transactionManager)
            .reader(anniversaryItemReader)
            .processor(compositeItemProcessor)
            .writer(anniversaryItemWriter)
            .build()
    }

    @Bean
    fun removeStep(
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
    fun anniversaryJob(jobRepository: JobRepository, anniversaryStep: Step, removeStep: Step): Job {
        return JobBuilder("anniversaryJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(anniversaryStep)
            .next(removeStep)
            .build()
    }

    companion object {
        private const val FCM_PAGE_SIZE = 10
        private const val FCM_CHUNK_SIZE = 10
    }
}
