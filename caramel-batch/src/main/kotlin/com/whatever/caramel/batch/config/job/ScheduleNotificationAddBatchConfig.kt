package com.whatever.caramel.batch.config.job

import com.whatever.caramel.common.util.DateTimeUtil
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
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDate

@Configuration
class ScheduleNotificationAddBatchConfig(
    private val entityManagerFactory: EntityManagerFactory,
    private val messageProvider: NotificationMessageProvider,
    private val scheduledNotificationRepository: ScheduledNotificationRepository,
) {
    @Bean
    @StepScope
    fun userBirthdayItemReader(
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @Value("#{jobParameters[runDate]}") runDate: LocalDate,
    ): JpaPagingItemReader<User> {
        val tomorrow = runDate.plusDays(1)

        val year = tomorrow.year
        val month = tomorrow.monthValue
        val day = tomorrow.dayOfMonth

        // 윤년이 아니고 2.28 일이면 2.29 도 같이 조회
        val leapYearPredicate = DateTimeUtil.isLeapYear(year).not() && month == 2 && day == 28
        val query = if (leapYearPredicate) {
            """
                SELECT DISTINCT u FROM User u
                JOIN u._couple c
                WHERE (function('TO_CHAR', u.birthDate, 'MM') = '02' AND function('TO_CHAR', u.birthDate, 'DD') = '28')
                OR (function('TO_CHAR', u.birthDate, 'MM') = '02' AND function('TO_CHAR', u.birthDate, 'DD') = '29')
                ORDER BY u.id
            """.trimIndent()
        } else {
            """
                SELECT DISTINCT u FROM User u
                JOIN u._couple c
                WHERE function('TO_CHAR', u.birthDate, 'MM') = :month
                AND function('TO_CHAR', u.birthDate, 'DD') = :day
                ORDER BY u.id
            """.trimIndent()
        }

        return JpaPagingItemReader<User>().apply {
            name = "userBirthdayReader"
            pageSize = ADD_PAGE_SIZE
            setEntityManagerFactory(entityManagerFactory)
            setQueryString(query)
            if (leapYearPredicate.not()) {
                setParameterValues(
                    mapOf("month" to String.format("%02d", month), "day" to String.format("%02d", day))
                )
            }
            setTransacted(false)
            afterPropertiesSet()
        }
    }

    @Bean
    @StepScope
    fun userBirthdayItemProcessor(
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @Value("#{jobParameters[runDate]}") runDate: LocalDate,
    ): ItemProcessor<User, List<ScheduledNotification>> {
        return ItemProcessor<User, List<ScheduledNotification>> { user ->
            val partner = user.couple?.members?.find { it.id != user.id } ?: return@ItemProcessor null

            val birthDate = user.birthDate ?: return@ItemProcessor null
            val thisYearsBirthday = birthDate.withYear(runDate.year)

            // 하루 전에 알림을 날릴 예정
            val notifyAt = thisYearsBirthday.minusDays(1).atStartOfDay()

            val nickname = user.nickname ?: return@ItemProcessor null
            val birthdayUserMessage = messageProvider.provide(
                type = NotificationType.MY_BIRTHDAY,
                notificationMessageParameter = BirthDayParameter(
                    label = "생일",
                    birthdayMemberNickname = nickname,
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
                    birthdayMemberNickname = nickname,
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
            scheduledNotificationRepository.insertAllWithoutConflict(chunk.flatten())
        }
    }

    @Bean
    fun userBirthdayAddStep(
        transactionManager: PlatformTransactionManager,
        whatEverJobRepository: JobRepository,
        userBirthdayItemReader: JpaPagingItemReader<User>,
        userBirthdayItemProcessor: ItemProcessor<User, List<ScheduledNotification>>,
        userBirthdayAddItemWriter: ItemWriter<List<ScheduledNotification>>,
    ): Step {
        return StepBuilder("addStep", whatEverJobRepository)
            .chunk<User, List<ScheduledNotification>>(ADD_CHUNK_SIZE, transactionManager)
            .reader(userBirthdayItemReader)
            .processor(userBirthdayItemProcessor)
            .writer(userBirthdayAddItemWriter)
            .build()
    }

    @Bean
    fun notificationAddJob(jobRepository: JobRepository, userBirthdayAddStep: Step): Job {
        return JobBuilder("notificationAddJob", jobRepository)
            .start(userBirthdayAddStep)
            .build()
    }

    companion object {
        private const val ADD_PAGE_SIZE = 10
        private const val ADD_CHUNK_SIZE = 10
    }
}
