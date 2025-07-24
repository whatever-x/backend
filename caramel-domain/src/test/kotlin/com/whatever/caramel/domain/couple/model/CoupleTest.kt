package com.whatever.caramel.domain.couple.model

import com.whatever.caramel.domain.couple.exception.CoupleExceptionCode.ILLEGAL_MEMBER_SIZE
import com.whatever.caramel.domain.couple.exception.CoupleIllegalArgumentException
import com.whatever.caramel.domain.couple.exception.CoupleIllegalStateException
import com.whatever.caramel.domain.user.model.LoginPlatform
import com.whatever.caramel.domain.user.model.User
import com.whatever.caramel.domain.user.model.UserStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class CoupleTest {

    @DisplayName("커플에 멤버를 추가할 경우 커플과 유저 양방향으로 등록된다.")
    @Test
    fun addMembers() {
        // given
        val couple = Couple()
        val user1 = User(
            id = 1L,
            platform = LoginPlatform.KAKAO,
            platformUserId = "test1",
            userStatus = UserStatus.SINGLE,
        )
        val user2 = User(
            id = 2L,
            platform = LoginPlatform.KAKAO,
            platformUserId = "test2",
            userStatus = UserStatus.SINGLE,
        )

        // when
        couple.addMembers(user1, user2)

        // then
        assertThat(couple.members).containsExactlyInAnyOrder(user1, user2)
        assertThat(couple.status).isEqualTo(CoupleStatus.ACTIVE)
        assertThat(user1.couple).isNotNull
        assertThat(user2.couple).isNotNull
    }

    @DisplayName("커플을 같은 유저만으로 구성할 경우 예외가 발생한다.")
    @Test
    fun addMembers_WithSameUser() {
        // given
        val couple = Couple()
        val user = User(
            id = 1L,
            platform = LoginPlatform.KAKAO,
            platformUserId = "test1",
            userStatus = UserStatus.SINGLE,
        )

        // when
        val result = assertThrows<CoupleIllegalArgumentException> { couple.addMembers(user, user) }

        // then
        assertThat(result.errorCode).isEqualTo(ILLEGAL_MEMBER_SIZE)
    }

    @DisplayName("커플멤버가 구성된 상태에서 추가로 등록을 할 경우 예외가 발생한다.")
    @Test
    fun addMembers_WithFullCouple() {
        // given
        val couple = Couple()
        val user1 = User(
            id = 1L,
            platform = LoginPlatform.KAKAO,
            platformUserId = "test1",
            userStatus = UserStatus.SINGLE,
        )
        val user2 = User(
            id = 2L,
            platform = LoginPlatform.KAKAO,
            platformUserId = "test2",
            userStatus = UserStatus.SINGLE,
        )
        couple.addMembers(user1, user2)

        val user3 = User(
            id = 3L,
            platform = LoginPlatform.KAKAO,
            platformUserId = "test3",
            userStatus = UserStatus.SINGLE,
        )

        // when
        val result = assertThrows<CoupleIllegalStateException> { couple.addMembers(user3, user2) }

        // then
        assertThat(result.errorCode).isEqualTo(ILLEGAL_MEMBER_SIZE)
    }
}
