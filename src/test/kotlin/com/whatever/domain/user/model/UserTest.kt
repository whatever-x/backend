package com.whatever.domain.user.model

import com.whatever.domain.couple.model.Couple
import com.whatever.domain.user.exception.UserExceptionCode.INVALID_BIRTH_DATE
import com.whatever.domain.user.exception.UserExceptionCode.INVALID_USER_STATUS_FOR_COUPLING
import com.whatever.domain.user.exception.UserIllegalArgumentException
import com.whatever.domain.user.exception.UserIllegalStateException
import com.whatever.util.DateTimeUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.Test

class UserTest {

    @DisplayName("SINGLE이 아닌 유저가 커플을 등록할 경우 예외를 반환한다.")
    @ParameterizedTest
    @CsvSource("NEW", "COUPLED")
    fun addMembers_WithIllegalStatusUser(illegalStatus: UserStatus) {
        // given
        val couple = Couple()
        val illegalStatusUser = User(
            platform = LoginPlatform.KAKAO,
            platformUserId = "test1",
            userStatus = illegalStatus,
        )

        // when
        val result = assertThrows<UserIllegalStateException> {
            illegalStatusUser.setCouple(couple)
        }

        // then
        assertThat(result.errorCode).isEqualTo(INVALID_USER_STATUS_FOR_COUPLING)
    }

    @DisplayName("생일 Date는 반드시 과거여야 한다.")
    @Test
    fun updateBirthDate() {
        // given
        val user = User(platform = LoginPlatform.TEST, platformUserId = "test-p-id")
        val requestTimeZone = DateTimeUtil.KST_ZONE_ID
        val pastDate = DateTimeUtil.zonedNow(requestTimeZone).minusDays(1).toLocalDate()  // valid

        // when
        user.updateBirthDate(pastDate, requestTimeZone)


        // then
        assertThat(user.birthDate).isEqualTo(pastDate)
    }

    @DisplayName("미래 시간으로 생일을 설정할 경우 예외를 반환한다.")
    @Test
    fun updateBirthDate_withFutureDate() {
        // given
        val user = User(platform = LoginPlatform.TEST, platformUserId = "test-p-id")
        val requestTimeZone = DateTimeUtil.KST_ZONE_ID
        val futureDate = DateTimeUtil.zonedNow(requestTimeZone).plusDays(1).toLocalDate()  // invalid

        // when
        val exception = assertThrows<UserIllegalArgumentException> {
            user.updateBirthDate(futureDate, requestTimeZone)
        }

        // then
        assertThat(exception.errorCode).isEqualTo(INVALID_BIRTH_DATE)
    }

}