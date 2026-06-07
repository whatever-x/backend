package com.whatever.caramel.batch.e2e

import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.domain.firebase.service.FirebaseService
import com.whatever.caramel.domain.notification.repository.ScheduledNotificationRepository
import com.whatever.caramel.infrastructure.firebase.exception.FcmSendException
import com.whatever.caramel.infrastructure.firebase.exception.FcmSendFailedReason
import com.whatever.caramel.infrastructure.firebase.exception.FirebaseExceptionCode
import jakarta.annotation.PostConstruct
import org.assertj.core.api.Assertions.assertThat
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
import java.time.ZoneId

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class AnniversaryJobTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val jdbcTemplate: JdbcTemplate,
    private val anniversaryJob: Job,
    private val scheduledNotificationRepository: ScheduledNotificationRepository,
) {
    @MockitoBean
    lateinit var firebaseService: FirebaseService

    @PostConstruct
    fun setUp() {
        jobLauncherTestUtils.job = anniversaryJob
    }

    @Test
    fun `anniversaryJob 배치 성공 테스트`() {
        insertScheduleNotification()

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

        jobExecution.stepExecutions.map { stepExecution ->
            if (stepExecution.stepName == "anniversaryStep") {
                // 4 개의 FCM 전송
                assertThat(stepExecution.writeCount).isEqualTo(4)
            }
        }
    }

    @Test
    fun `anniversaryJob 중 firebase 토큰이 FCM_UNREGISTERED_TOKEN 를 받으면 removeToken이 호출된다`() {
        jdbcTemplate.batchUpdate(
            "INSERT INTO scheduled_notification (target_user_id, notification_type, notify_at, title, body, image, created_at, updated_at)" +
                "VALUES (?, 'MY_BIRTHDAY', CURRENT_TIMESTAMP, 'title', 'body', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            listOf(
                arrayOf(1L),
            )
        )
        val invalidToken = "invalid-token"

        whenever(firebaseService.sendNotification(any(), any()))
            .thenThrow(
                FcmSendException(
                    tokens = listOf(
                        FcmSendFailedReason(
                            errorToken = invalidToken,
                            errorMessageCode = FirebaseExceptionCode.FCM_UNREGISTERED_TOKEN,
                        )
                    ),
                    errorCode = FirebaseExceptionCode.FCM_UNREGISTERED_TOKEN,
                    errorUi = ErrorUi.Toast("알림을 전송에 실패했어요.")
                )
            )

        val jobParameters = jobLauncherTestUtils.uniqueJobParametersBuilder
            .addString("runDate", LocalDate.now(ZoneId.of("Asia/Seoul")).toString())
            .toJobParameters()

        // 기존에 1개가 들어있다고 가정
        val before = scheduledNotificationRepository.findAll()
        assertThat(before.count()).isEqualTo(1)

        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus).isEqualTo(ExitStatus.COMPLETED)

        verify(firebaseService, times(1))
            .removeToken(invalidToken)
    }

    private fun insertScheduleNotification() {
        jdbcTemplate.batchUpdate(
            "INSERT INTO scheduled_notification (target_user_id, notification_type, notify_at, title, body, image, created_at, updated_at)" +
                "VALUES (?, 'MY_BIRTHDAY', CURRENT_TIMESTAMP, 'title', 'body', null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            listOf(
                arrayOf(1L),
                arrayOf(2L),
                arrayOf(3L),
                arrayOf(4L),
            )
        )
    }
}
