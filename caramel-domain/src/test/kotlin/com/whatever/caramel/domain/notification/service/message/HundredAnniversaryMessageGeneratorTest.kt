package com.whatever.caramel.domain.notification.service.message

import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.vo.NotificationMessageVo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test

class HundredAnniversaryMessageGeneratorTest {
    private val generator = HundredAnniversaryMessageGenerator()

    @DisplayName("주어진 x00일 기념일 파라미터로 기념일에 대한 메시지를 생성한다")
    @Test
    fun generateMessage() {
        // given
        val parameter = HundredAnniversaryParameter(label = "100일")
        val expectedMessage = NotificationMessageVo(
            type = NotificationType.ANNIVERSARY_HUNDRED,
            title = "내일은 ${parameter.label}이에요!",
            body = "함께 쌓아온 소중한 시간, 앞으로도 예쁘게 채워가요!"
        )

        // when
        val result = generator.generate(parameter)

        // then
        assertThat(result).isEqualTo(expectedMessage)
    }
}
