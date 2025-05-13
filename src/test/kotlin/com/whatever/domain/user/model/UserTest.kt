package com.whatever.domain.user.model

import com.whatever.domain.couple.model.Couple
import com.whatever.domain.user.exception.UserExceptionCode.INVALID_USER_STATUS_FOR_COUPLING
import com.whatever.domain.user.exception.UserIllegalStateException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

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

}