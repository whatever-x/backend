package com.whatever.caramel.domain.notification.service.message

import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.vo.NotificationMessageVo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test

class PartnerBirthdayMessageGeneratorTest {
    private val generator = PartnerBirthdayMessageGenerator()

    @DisplayName("주어진 생일 파라미터로 연인의 생일에 대한 메시지를 생성한다")
    @Test
    fun generateMessage() {
        // given
        val parameter = BirthDayParameter(
            label = "test label",
            birthdayMemberNickname = "김생일",
            isMyBirthday = true
        )
        val expectedMessage = NotificationMessageVo(
            type = NotificationType.PARTNER_BIRTHDAY,
            title = "내일은 ${parameter.birthdayMemberNickname}님의 생일이에요! \uD83E\uDD73",  // 🥳
            body = "잊지 말고 축하의 마음을 전하는건 어떨까요?",
        )

        // when
        val result = generator.generate(parameter)

        // then
        assertThat(result).isEqualTo(expectedMessage)
    }
}
