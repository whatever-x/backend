package com.whatever.caramel.api.firebase.service

import com.google.firebase.messaging.BatchResponse
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Notification
import com.whatever.domain.firebase.exception.FcmIllegalArgumentException
import com.whatever.domain.firebase.exception.FirebaseExceptionCode.FCM_EMPTY_TOKEN
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlin.test.Test

class FcmSenderTest {

    private val fcmSender = FcmSender()

    private lateinit var firebaseAppMock: AutoCloseable

    @BeforeEach
    fun setup() {
        firebaseAppMock = mockStatic(FirebaseMessaging::class.java)
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

        val notif = Notification.builder().setTitle("title").setBody("body").build()

        // when
        val result = fcmSender.sendNotification("test-token", notif)

        // then
        assertThat(result).isEqualTo(messageId)
    }

    @DisplayName("다수에게 FCM Notification 전달 시 BatchResponse가 반환된다.")
    @Test
    fun sendNotificationAll() {
        // given
        val fakeMessaging = Mockito.mock(FirebaseMessaging::class.java)
        whenever(FirebaseMessaging.getInstance()).thenReturn(fakeMessaging)

        val fakeResponse = Mockito.mock(BatchResponse::class.java)
        whenever(fakeMessaging.sendEachForMulticast(any())).thenReturn(fakeResponse)

        val notif = Notification.builder().setTitle("title").setBody("body").build()
        val tokens = listOf("token-1", "token-2", "token-3")

        // when
        val result = fcmSender.sendNotificationAll(tokens, notif)

        // then
        assertThat(result).isSameAs(fakeResponse)
    }

    @DisplayName("다수에게 FCM Notification 전달 시 토큰이 없다면 예외를 반환한다.")
    @Test
    fun sendNotificationAll_WithEmptyToken() {
        // given
        val notif = Notification.builder().setTitle("title").setBody("body").build()

        // when, then
        val exception = assertThrows<FcmIllegalArgumentException> {
            fcmSender.sendNotificationAll(emptyList(), notif)
        }
        assertThat(exception.errorCode).isEqualTo(FCM_EMPTY_TOKEN)
    }

    @DisplayName("FCM Data 전달 시 Message ID가 반환된다.")
    @Test
    fun sendDataAll() {
        // given
        val fakeMessaging = Mockito.mock(FirebaseMessaging::class.java)
        whenever(FirebaseMessaging.getInstance()).thenReturn(fakeMessaging)

        val fakeResponse = Mockito.mock(BatchResponse::class.java)
        whenever(fakeMessaging.sendEachForMulticast(any())).thenReturn(fakeResponse)

        val data = mapOf("key" to "value")
        val tokens = listOf("token-1", "token-2", "token-3")

        // when
        val result = fcmSender.sendDataAll(tokens, data)

        // then
        assertThat(result).isSameAs(fakeResponse)
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
        assertThat(exception.errorCode).isEqualTo(FCM_EMPTY_TOKEN)
    }
}
