package com.whatever.caramel.infrastructure.openfeign

import com.google.firebase.messaging.FirebaseMessaging
import com.whatever.caramel.infrastructure.firebase.FcmSender
import com.whatever.caramel.infrastructure.firebase.exception.FcmIllegalArgumentException
import com.whatever.caramel.infrastructure.firebase.exception.FirebaseExceptionCode
import com.whatever.caramel.infrastructure.firebase.model.FcmNotification
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test

class FcmSenderTest {

    private val fcmSender = FcmSender()

    private lateinit var firebaseAppMock: AutoCloseable

    @BeforeEach
    fun setup() {
        firebaseAppMock = Mockito.mockStatic(FirebaseMessaging::class.java)
    }

    @AfterEach
    fun teardown() {
        firebaseAppMock.close()
    }

    @DisplayName("FCM Notification 전달 시 message ID가 반환된다.")
    @Test
    fun sendNotification() {
        // given
        val fakeMessaging = Mockito.mock(FirebaseMessaging::class.java)
        whenever(FirebaseMessaging.getInstance()).thenReturn(fakeMessaging)

        val messageId = "message-id"
        whenever(fakeMessaging.send(any())).thenReturn(messageId)
        val notification = FcmNotification(title = "title", body = "body")

        // when
        val result = fcmSender.sendNotification("test-token", notification)

        // then
        Assertions.assertThat(result).isEqualTo(messageId)
    }

    @DisplayName("다수에게 FCM Notification을 전달시 sendEachForMulticast 실행") // 테스트의 목적을 더 명확하게 변경
    @Test
    fun sendNotificationAll() {
        // given
        val fakeMessaging = Mockito.mock(FirebaseMessaging::class.java)
        whenever(FirebaseMessaging.getInstance()).thenReturn(fakeMessaging)

        val notification = FcmNotification(title = "title", body = "body")
        val tokens = listOf("token-1", "token-2", "token-3")

        // when
        fcmSender.sendNotificationAll(tokens, notification)

        // then
        verify(fakeMessaging).sendEachForMulticast(any())
    }

    @DisplayName("다수에게 FCM Notification 전달 시 토큰이 없다면 예외를 반환한다.")
    @Test
    fun sendNotificationAll_WithEmptyToken() {
        // given
        val notification = FcmNotification(title = "title", body = "body")

        // when, then
        val exception = assertThrows<FcmIllegalArgumentException> {
            fcmSender.sendNotificationAll(emptyList(), notification)
        }
        assertThat(exception.errorCode).isEqualTo(FirebaseExceptionCode.FCM_EMPTY_TOKEN)
    }

    @DisplayName("FCM Data 전달 시 sendEachForMulticast 실행")
    @Test
    fun sendDataAll() {
        // given
        val fakeMessaging = Mockito.mock(FirebaseMessaging::class.java)
        whenever(FirebaseMessaging.getInstance()).thenReturn(fakeMessaging)

        val data = mapOf("key" to "value")
        val tokens = listOf("token-1", "token-2", "token-3")

        // when
        fcmSender.sendDataAll(tokens, data)

        // then
        verify(fakeMessaging).sendEachForMulticast(any())
    }

    @DisplayName("다수에게 FCM Data 전달 시 BatchResponse가 반환된다.")
    @Test
    fun sendDataAll_WithEmptyToken() {
        // given
        val data = mapOf("key" to "value")

        // when, then
        val exception = assertThrows<FcmIllegalArgumentException> {
            fcmSender.sendDataAll(emptyList(), data)
        }
        assertThat(exception.errorCode).isEqualTo(FirebaseExceptionCode.FCM_EMPTY_TOKEN)
    }
}