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
import org.springframework.batch.item.support.CompositeItemProcessor
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
        val localDate = LocalDate.now(zoneSource)
        val startOfBirthDay = localDate.atStartOfDay(zoneSource).toLocalDateTime()
        val endOfBirthDay = localDate.atTime(23, 59, 59).withNano(0)

        return JpaPagingItemReader<User>().apply {
            setEntityManagerFactory(entityManagerFactory)
            setQueryString("SELECT u FROM User u WHERE u.birthDate BETWEEN :startOfDay AND :endOfDay")
            setParameterValues(mapOf("startOfBirthDay" to startOfBirthDay, "endOfBirthDay" to endOfBirthDay))
            pageSize = DEFAULT_BATCH_SIZE
            afterPropertiesSet()
        }
    }

    @Bean
    @StepScope
    fun userBirthdayListItemProcessor(): ItemProcessor<User, List<ScheduledNotification>> {
        return ItemProcessor<User, List<ScheduledNotification>> { user ->
            val zoneSource = ZoneId.of("Asia/Seoul")
            val birthdayUser = user.couple?.members?.find { it.birthDate == LocalDate.now() }
                ?: return@ItemProcessor emptyList()
            val partner = user.couple?.members?.find { it.id != birthdayUser.id }
                ?: return@ItemProcessor emptyList()

            val notifyAt = birthdayUser.birthDate?.atStartOfDay(zoneSource)?.toLocalDateTime()
                ?: return@ItemProcessor emptyList()

            val birthdayUserMessage = messageProvider.provide(
                type = NotificationType.MY_BIRTHDAY,
                notificationMessageParameter = BirthDayParameter(
                    label = "생일",
                    birthdayMemberNickname = requireNotNull(birthdayUser.nickname),
                    isMyBirthday = true
                )
            )

            val birthDayUserScheduleNotification = ScheduledNotification(
                targetUserId = birthdayUser.id,
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
                    birthdayMemberNickname = requireNotNull(birthdayUser.nickname),
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
    fun userBirthdayItemProcessor(): ItemProcessor<List<ScheduledNotification>, ScheduledNotification> {
        return ItemProcessor<List<ScheduledNotification>, ScheduledNotification> {
            val iterator = it.iterator()
            if (iterator.hasNext()) {
                iterator.next()
            } else {
                null
            }
        }
    }

    @Bean
    @StepScope
    fun compositeProcessor(
        userBirthdayListItemProcessor: ItemProcessor<User, List<ScheduledNotification>>,
        userBirthdayItemProcessor: ItemProcessor<List<ScheduledNotification>, ScheduledNotification>,
    ): CompositeItemProcessor<User, ScheduledNotification> {
        return CompositeItemProcessor<User, ScheduledNotification>().apply {
            setDelegates(
                listOf(userBirthdayListItemProcessor, userBirthdayItemProcessor)
            )
        }
    }

    @Bean
    @StepScope
    fun userBirthdayAddItemWriter(): ItemWriter<ScheduledNotification> {
        return ItemWriter { chunk ->
            scheduledNotificationRepository.saveAll(chunk)
        }
    }

    @Bean
    fun userBirthdayAddStep(
        whatEverJobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        userBirthdayItemReader: ItemReader<User>,
        compositeProcessor: ItemProcessor<User, ScheduledNotification>,
        userBirthdayAddItemWriter: ItemWriter<ScheduledNotification>,
    ): Step {
        return StepBuilder("add", whatEverJobRepository)
            .chunk<User, ScheduledNotification>(DEFAULT_BATCH_SIZE, transactionManager)
            .reader(userBirthdayItemReader)
            .processor(compositeProcessor)
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
