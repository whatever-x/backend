package com.whatever.caramel.domain.notification.service.message

import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.vo.NotificationMessageVo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test

class MyBirthdayMessageGeneratorTest {
    private val generator = MyBirthdayMessageGenerator()

    @DisplayName("주어진 생일 파라미터로 내 생일에 대한 메시지를 생성한다")
    @Test
    fun generateMessage() {
        // given
        val parameter = BirthDayParameter(
            label = "test label",
            birthdayMemberNickname = "test nick",
            isMyBirthday = true
        )
        val expectedMessage = NotificationMessageVo(
            type = NotificationType.MY_BIRTHDAY,
            title = "내일은 기다리던 나의 생일! \uD83C\uDF82",  // 🎂
            body = "가장 특별한 하루, 즐겁고 행복하게 보내세요!",
        )

        // when
        val result = generator.generate(parameter)

        // then
        assertThat(result).isEqualTo(expectedMessage)
    }
}
