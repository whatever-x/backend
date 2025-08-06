package com.whatever.caramel.domain.notification.service.message

import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.vo.NotificationMessageVo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test

class YearlyAnniversaryMessageGeneratorTest {
    private val generator = YearlyAnniversaryMessageGenerator()

    @DisplayName("주어진 N주년 기념일 파라미터로 기념일에 대한 메시지를 생성한다")
    @Test
    fun generateMessage() {
        // given
        val parameter = YearlyAnniversaryParameter(label = "1주년")
        val expectedMessage = NotificationMessageVo(
            type = NotificationType.ANNIVERSARY_YEARLY,
            title = "내일은 ${parameter.label} 기념일이에요!",
            body = "함께여서 모든 계절이 특별했지 않나요? 내일은 더 소중한 하루를 만들어요.",
        )

        // when
        val result = generator.generate(parameter)

        // then
        assertThat(result).isEqualTo(expectedMessage)
    }
}