package com.whatever.domain.base

import com.whatever.CaramelDomainSpringBootTest
import com.whatever.caramel.common.global.exception.GlobalExceptionCode
import com.whatever.caramel.common.global.exception.common.CaramelException
import com.whatever.caramel.common.global.exception.common.CaramelExceptionCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.annotation.Profile
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Component
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import kotlin.test.Test

@CaramelDomainSpringBootTest
class AbstractEntityCleanupServiceTest {

    @MockitoSpyBean
    private lateinit var cleanupService: FakeEntityCleanupService

    @DisplayName("cleanupEntity()에서 DataAccessException 발생하면 최대 3번까지 재시도한다.")
    @Test
    fun cleanupEntity_WithSuccessAfterRetry() {
        // given
        whenever(cleanupService.runCleanup(any()))
            .thenThrow(FakeDataAccessException("first fail"))
            .thenThrow(FakeDataAccessException("second fail"))
            .thenReturn(EFFECTED_ROW_COUNT)

        // when
        cleanupService.cleanupEntity(USER_ID, ENTITY_NAME)

        // then
        verify(cleanupService, times(3))
            .cleanupEntity(any(), any())
        verify(cleanupService, never())
            .recoverCleanupEntity(any(), any(), any())
    }

    @DisplayName("cleanupEntity()에서 DataAccessException이 세번 발생하면 복구 메서드에 진입하고 예외 발생한다.")
    @Test
    fun cleanupEntity_WithFail() {
        // given
        whenever(cleanupService.runCleanup(any()))
            .thenThrow(FakeDataAccessException("first fail"))
            .thenThrow(FakeDataAccessException("second fail"))
            .thenThrow(FakeDataAccessException("third fail"))

        // when
        val result = assertThrows<FakeDataAccessException> {
            cleanupService.cleanupEntity(USER_ID, ENTITY_NAME)
        }

        // then
        assertThat(result.message).isEqualTo("third fail")
        verify(cleanupService, times(3))
            .cleanupEntity(any(), any())
    }

    @DisplayName("cleanupEntity()에서 DataAccessException 이외의 예외가 발생하면 바로 복구 메서드로 진입하고, 예외가 발생한다.")
    @Test
    fun cleanupEntity_WithFailByNonDataAccessException() {
        // given
        whenever(cleanupService.runCleanup(any()))
            .thenThrow(FakeNonRetryException(GlobalExceptionCode.UNKNOWN))

        // when
        val result = assertThrows<FakeNonRetryException> {
            cleanupService.cleanupEntity(USER_ID, ENTITY_NAME)
        }

        // then
        assertThat(result.errorCode).isEqualTo(GlobalExceptionCode.UNKNOWN)
        verify(cleanupService, times(1))
            .cleanupEntity(any(), any())
    }

    companion object {
        const val USER_ID = 0L
        const val ENTITY_NAME = "Fake"
        const val EFFECTED_ROW_COUNT = 0
    }
}

@Profile("test")
@Component
class FakeEntityCleanupService : AbstractEntityCleanupService<FakeEntity>() {
    public override fun runCleanup(userId: Long): Int {
        // 모킹을 통해 해당 메서드를 제어한다.
        return 0
    }
}

class FakeEntity : BaseEntity()
class FakeDataAccessException(msg: String) : DataAccessException(msg)
class FakeNonRetryException(errorCode: CaramelExceptionCode) : CaramelException(errorCode)
