package com.whatever.caramel.batch.e2e

import com.whatever.caramel.domain.notification.repository.ScheduledNotificationRepository
import com.whatever.caramel.domain.user.repository.UserRepository
import jakarta.annotation.PostConstruct
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.ZoneId

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class ScheduleNotificationAddBatchTest @Autowired constructor(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val jdbcTemplate: JdbcTemplate,
    private val notificationAddJob: Job,
    private val scheduledNotificationRepository: ScheduledNotificationRepository,
) {

    @PostConstruct
    fun setUp() {
        jobLauncherTestUtils.job = notificationAddJob
        insertCouples()
        insertUsers()
    }

    @Test
    fun `ScheduleNotificationAdd 배치 실행 성공 테스트`() {
        val jobParameters = jobLauncherTestUtils.uniqueJobParametersBuilder
            .addString("runDate", LocalDate.now(ZoneId.of("Asia/Seoul")).toString())
            .toJobParameters()

        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(jobExecution.exitStatus).isEqualTo(ExitStatus.COMPLETED)

        val result = scheduledNotificationRepository.findAll()

        // 나 and 연인한테 메시지
        assertThat(result.count()).isEqualTo(4)
    }

    private fun insertCouples() {
        // 커플 초기화
        jdbcTemplate.batchUpdate(
            "INSERT INTO couple (shared_message, status, is_deleted, version, created_at, updated_at) VALUES (?, ?, false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            listOf(
                arrayOf("hi", "ACTIVE"),
                arrayOf("hi", "ACTIVE"),
                arrayOf("hi", "ACTIVE"),
                arrayOf("hi", "ACTIVE"),
                arrayOf("hi", "ACTIVE"),
            )
        )
    }

    private fun insertUsers() {
        // 유저 초기화
        val today = LocalDate.now()
        val users = listOf(
            Triple(today.minusDays(3), 1, "pita" to "my-user-id1"),
            Triple(today.minusDays(3), 1, "pita2" to "my-user-id2"),
            Triple(today.minusDays(2), 2, "pita3" to "my-user-id3"),
            Triple(today.minusDays(2), 2, "pita4" to "my-user-id4"),
            Triple(today.minusDays(1), 3, "pita5" to "my-user-id5"),
            Triple(today.minusDays(1), 3, "pita6" to "my-user-id6"),
            Triple(today, 4, "pita7" to "my-user-id7"),
            Triple(today, 5, "pita8" to "my-user-id8"),
            Triple(today.plusDays(1), 4, "pita9" to "my-user-id9"),
            Triple(today.plusDays(1), 5, "pita10" to "my-user-id10"),
        )

        users.forEach { (date, coupleId, nickIdPair) ->
            val (nickname, platformUserId) = nickIdPair
            jdbcTemplate.update(
                """
                INSERT INTO "user" 
                (birth_date, is_deleted, couple_id, nickname, gender, user_status, platform, platform_user_id, created_at, updated_at)
                VALUES (?, FALSE, ?, ?, 'MALE', 'COUPLED', 'KAKAO', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """.trimIndent(),
                date, coupleId, nickname, platformUserId
            )
        }
    }
}
