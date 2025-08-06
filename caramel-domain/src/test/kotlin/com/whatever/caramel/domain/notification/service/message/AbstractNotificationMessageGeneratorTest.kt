package com.whatever.caramel.domain.notification.service.message

import com.whatever.caramel.domain.notification.exception.InvalidMessageParameterException
import com.whatever.caramel.domain.notification.exception.NotificationExceptionCode.INVALID_MESSAGE_PARAMETER
import com.whatever.caramel.domain.notification.model.NotificationType
import com.whatever.caramel.domain.notification.vo.NotificationMessageVo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class AbstractNotificationMessageGeneratorTest {

    private val generator = TestGenerator()

    @DisplayName("구현체가 기대하는 파라미터를 사용하여 상황에 맞는 메시지를 생성한다")
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
            title = "test title",
            body = "test body"
        )

        // when
        val result = generator.generate(parameter)

        // then
        assertThat(result).isEqualTo(expectedMessage)
    }

    @DisplayName("구현체가 기대하는 파라미터가 아닌 파라미터가 주어지면 예외를 반환한다")
    @Test
    fun generateMessage_WithInvalidParameter_ThenThrowException() {
        // given
        val invalidParameter = HundredAnniversaryParameter(
            label = "test label",
        )

        // when
        val result = assertThrows<InvalidMessageParameterException> {
            generator.generate(invalidParameter)
        }

        // then
        assertThat(result.errorCode).isEqualTo(INVALID_MESSAGE_PARAMETER)
    }

}

private class TestGenerator
    : AbstractNotificationMessageGenerator<BirthDayParameter>(BirthDayParameter::class) {
    override fun supports(): NotificationType = NotificationType.MY_BIRTHDAY
    override fun generateMessage(parameter: BirthDayParameter): NotificationMessageVo {
        return NotificationMessageVo(
            type = supports(),
            title = "test title",
            body = "test body"
        )
    }

}
