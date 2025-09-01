package com.whatever.caramel.batch.config

import com.whatever.caramel.batch.config.BatchConfig.Companion.DEFAULT_BATCH_SIZE
import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.domain.notification.repository.ScheduledNotificationRepository
import com.whatever.caramel.domain.notification.service.message.BirthDayParameter
import com.whatever.caramel.domain.notification.service.message.NotificationMessageProvider
import com.whatever.caramel.domain.user.model.User
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
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDate
import java.time.ZoneId

@Configuration
class ScheduleNotificationAddBatchConfig(
    private val messageProvider: NotificationMessageProvider,
    private val scheduledNotificationRepository: ScheduledNotificationRepository,
) {
    @Bean
    @StepScope
    fun userBirthdayItemReader(entityManagerFactory: EntityManagerFactory): JpaPagingItemReader<User> {
        val zoneSource = ZoneId.of("Asia/Seoul")
        val expectedDate = LocalDate.now(zoneSource).plusDays(1)

        val month = expectedDate.monthValue
        val day = expectedDate.dayOfMonth

        return JpaPagingItemReader<User>().apply {
            setEntityManagerFactory(entityManagerFactory)
            setQueryString(
                """
                    SELECT DISTINCT u FROM User u
                    JOIN FETCH u._couple c
                    WHERE function('TO_CHAR', u.birthDate, 'MM') = LPAD(CAST(:month AS text), 2, '0')
                    AND function('TO_CHAR', u.birthDate, 'DD') = LPAD(CAST(:day AS text), 2, '0')
                    ORDER BY u.id
                """.trimIndent()
            )
            setParameterValues(
                mapOf("month" to month, "day" to day)
            )
            pageSize = DEFAULT_BATCH_SIZE
            afterPropertiesSet()
        }
    }

    @Bean
    @StepScope
    fun userBirthdayListItemProcessor(): ItemProcessor<User, User> {
        return ItemProcessor<User, User> { user ->
            user
        }
    }

    @Bean
    @StepScope
    fun userBirthdayItemProcessor(): ItemProcessor<User, List<ScheduledNotification>> {
        return ItemProcessor<User, List<ScheduledNotification>> { user ->
            val zoneSource = ZoneId.of("Asia/Seoul")

            val partner = user.couple?.members?.find { it.id != user.id }
                ?: return@ItemProcessor emptyList()

            val notifyAt = user.birthDate?.atStartOfDay(zoneSource)?.toLocalDateTime()?.minusDays(1)
                ?: return@ItemProcessor emptyList()

            val birthdayUserMessage = messageProvider.provide(
                type = NotificationType.MY_BIRTHDAY,
                notificationMessageParameter = BirthDayParameter(
                    label = "생일",
                    birthdayMemberNickname = requireNotNull(user.nickname),
                    isMyBirthday = true
                )
            )

            val birthDayUserScheduleNotification = ScheduledNotification(
                targetUserId = user.id,
                notificationType = NotificationType.MY_BIRTHDAY,
                notifyAt = notifyAt,
                title = birthdayUserMessage.title,
                body = birthdayUserMessage.body,
                image = null
            )

            val partnerMessage = messageProvider.provide(
                type = NotificationType.PARTNER_BIRTHDAY,
                notificationMessageParameter = BirthDayParameter(
                    label = "생일",
                    birthdayMemberNickname = requireNotNull(user.nickname),
                    isMyBirthday = false
                )
            )

            val partnerScheduleNotification = ScheduledNotification(
                targetUserId = partner.id,
                notificationType = NotificationType.PARTNER_BIRTHDAY,
                notifyAt = notifyAt,
                title = partnerMessage.title,
                body = partnerMessage.body,
                image = null
            )

            listOf(birthDayUserScheduleNotification, partnerScheduleNotification)
        }
    }

    @Bean
    @StepScope
    fun userBirthdayAddItemWriter(): ItemWriter<List<ScheduledNotification>> {
        return ItemWriter { chunk ->
            scheduledNotificationRepository.saveAll(chunk.flatten())
        }
    }

    @Bean
    fun userBirthdayAddStep(
        whatEverJobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        userBirthdayItemReader: ItemReader<User>,
        userBirthdayItemProcessor: ItemProcessor<User, List<ScheduledNotification>>,
        userBirthdayAddItemWriter: ItemWriter<List<ScheduledNotification>>,
    ): Step {
        return StepBuilder("add", whatEverJobRepository)
            .chunk<User, List<ScheduledNotification>>(DEFAULT_BATCH_SIZE, transactionManager)
            .reader(userBirthdayItemReader)
            .processor(userBirthdayItemProcessor)
            .writer(userBirthdayAddItemWriter)
            .build()
    }

    @Bean
    fun scheduleAddJob(jobRepository: JobRepository, userBirthdayAddStep: Step): Job {
        return JobBuilder("add", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(userBirthdayAddStep)
            .build()
    }
}
