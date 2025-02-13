package com.whatever.domain.auth.service.provider

import com.whatever.domain.auth.client.KakaoOAuthClient
import com.whatever.domain.auth.client.dto.KakaoAccount
import com.whatever.domain.auth.client.dto.KakaoUserInfoResponse
import com.whatever.domain.auth.client.dto.Profile
import com.whatever.domain.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@ActiveProfiles("test")
@SpringBootTest
class KakaoUserProviderTest @Autowired constructor(
    private val kakaoUserProvider: KakaoUserProvider,
    private val userRepository: UserRepository
) {

    @MockitoBean
    private lateinit var kakaoOAuthClient: KakaoOAuthClient

    @BeforeEach
    fun cleanDatabase() {
        userRepository.deleteAllInBatch()
    }

    @Test
    fun findOrCreateUserWithMultiThread() {
        Mockito.`when`(kakaoOAuthClient.getUserInfo(Mockito.anyString()))
            .thenReturn(
                KakaoUserInfoResponse(
                    id = 1L,
                    KakaoAccount(
                        Profile(
                            nickname = "caramel",
                            thumbnailImageUrl = "http://test.test.test/thumbnailImageUrl",
                            profileImageUrl = "http://test.test.test/profileImageUrl",
                        )
                    )
                )
            )

        // given
        val kakaoAccessToken = "test-kakao-access-token"

        val threadCount = 1000
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        val successCnt = AtomicInteger()
        val failCnt = AtomicInteger()

        // when
        repeat(threadCount) {
            executor.submit {
                try {
                    kakaoUserProvider.findOrCreateUser(kakaoAccessToken)
                    successCnt.incrementAndGet()
                } catch (e: Exception) {
                    failCnt.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await(10, TimeUnit.SECONDS).let {
            executor.shutdown()
        }

        // then
        userRepository.findAll().let {
            assertThat(it.size).isEqualTo(1)
        }

        assertThat(successCnt.toInt()).isEqualTo(threadCount)
        assertThat(failCnt.toInt()).isEqualTo(0)
    }
}