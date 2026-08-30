package com.whatever.caramel.batch.e2e

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.domain.firebase.service.FirebaseService
import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.model.ScheduledNotification
import com.whatever.caramel.domain.notification.model.SendStatus
import com.whatever.caramel.domain.notification.repository.NotificationHistoryRepository
import com.whatever.caramel.domain.notification.repository.ScheduledNotificationRepository
import com.whatever.caramel.infrastructure.firebase.exception.FcmSendException
import com.whatever.caramel.infrastructure.firebase.exception.FcmSendFailedReason
import com.whatever.caramel.infrastructure.firebase.exception.FirebaseExceptionCode
import jakarta.annotation.PostConstruct
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class AnniversaryJobTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val jdbcTemplate: JdbcTemplate,
    private val anniversaryJob: Job,
    private val scheduledNotificationRepository: ScheduledNotificationRepository,
    private val notificationHistoryRepository: NotificationHistoryRepository,
) {
    @MockitoBean
    lateinit var firebaseService: FirebaseService

    @PostConstruct
    fun setUp() {
        jobLauncherTestUtils.job = anniversaryJob
    }

    @BeforeEach
    fun cleanUp() {
        jdbcTemplate.update("DELETE FROM notification_history")
        jdbcTemplate.update("DELETE FROM scheduled_notification")
    }

    @Test
    fun `anniversaryJob 배치 성공 테스트`() {
        insertScheduleNotification()
        whenever(firebaseService.sendNotification(any(), any())).thenReturn(true)

        val jobParameters = jobLauncherTestUtils.uniqueJobParametersBuilder
            .addString("runDate", LocalDate.now(ZoneId.of("Asia/Seoul")).toString())
            .toJobParameters()

        // 기존에 4개가 들어있다고 가정
        val before = scheduledNotificationRepository.findAll()
        assertThat(before.count()).isEqualTo(4)

        // 배치 수행
        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus).isEqualTo(ExitStatus.COMPLETED)

        // 모두 처리 후 0개
        val result = scheduledNotificationRepository.findAll()
        assertThat(result.count()).isEqualTo(0)
        assertThat(notificationHistoryRepository.findAll()).isEmpty()

        verify(firebaseService, times(4)).sendNotification(any(), any())

        jobExecution.stepExecutions.map { stepExecution ->
            if (stepExecution.stepName == "anniversaryStep") {
                // 4 개의 FCM 전송
                assertThat(stepExecution.writeCount).isEqualTo(4)
            }
        }
    }

    @Test
    fun `anniversaryJob 중 firebase 토큰이 FCM_UNREGISTERED_TOKEN 를 받으면 removeToken이 호출된다`() {
        val notification = insertScheduleNotifications(count = 1).single()
        val invalidToken = "invalid-token"

        val exception = FcmSendException(
            tokens = listOf(
                FcmSendFailedReason(
                    errorToken = invalidToken,
                    errorMessageCode = FirebaseExceptionCode.FCM_UNREGISTERED_TOKEN,
                )
            ),
            errorCode = FirebaseExceptionCode.FCM_UNREGISTERED_TOKEN,
            errorUi = ErrorUi.Toast("알림을 전송에 실패했어요.")
        )

        whenever(firebaseService.sendNotification(any(), any()))
            .thenThrow(exception)

        val jobParameters = jobLauncherTestUtils.uniqueJobParametersBuilder
            .addString("runDate", LocalDate.now(ZoneId.of("Asia/Seoul")).toString())
            .toJobParameters()

        // 기존에 1개가 들어있다고 가정
        val before = scheduledNotificationRepository.findAll()
        assertThat(before.count()).isEqualTo(1)

        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus).isEqualTo(ExitStatus.COMPLETED)
        assertThat(scheduledNotificationRepository.findAll()).isEmpty()

        val history = notificationHistoryRepository.findAll().single()
        assertThat(history.sourceNotificationId).isEqualTo(notification.id)
        assertThat(history.sendStatus).isEqualTo(SendStatus.FAILED)
        assertThat(history.errorMessage).isNotBlank()

        verify(firebaseService, times(1))
            .removeUnregisteredTokens(exception)
    }

    @Test
    fun `anniversaryJob은 21건을 빠짐없이 처리한다`() {
        insertScheduleNotifications(count = 21)
        whenever(firebaseService.sendNotification(any(), any())).thenReturn(true)

        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters())

        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(scheduledNotificationRepository.findAll()).isEmpty()
        assertThat(notificationHistoryRepository.findAll()).isEmpty()
        verify(firebaseService, times(21)).sendNotification(any(), any())
    }

    private fun insertScheduleNotification() {
        insertScheduleNotifications(count = 4)
    }

    private fun insertScheduleNotifications(count: Int): List<ScheduledNotification> {
        val now = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
        return scheduledNotificationRepository.saveAll(
            (1..count).map { index ->
                ScheduledNotification(
                    targetUserId = index.toLong(),
                    notificationType = NotificationType.MY_BIRTHDAY,
                    notifyAt = now,
                    title = "title-$index",
                    body = "body-$index",
                )
            }
        )
    }

    private fun jobParameters() = jobLauncherTestUtils.uniqueJobParametersBuilder
        .addString("runDate", LocalDate.now(ZoneId.of("Asia/Seoul")).toString())
        .toJobParameters()
}
