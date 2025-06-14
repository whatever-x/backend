package com.whatever.domain.auth.service.provider

import com.whatever.domain.auth.client.KakaoOIDCClient
import com.whatever.domain.auth.client.dto.KakaoIdTokenPayload
import com.whatever.domain.auth.client.dto.OIDCPublicKeysResponse
import com.whatever.domain.auth.service.OIDCHelper
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
    private lateinit var kakaoOIDCClient: KakaoOIDCClient
    @MockitoBean
    private lateinit var oidcHelper: OIDCHelper

    @BeforeEach
    fun cleanDatabase() {
        userRepository.deleteAllInBatch()
    }

    @Test
    fun findOrCreateUserWithMultiThread() {
        Mockito.`when`(kakaoOIDCClient.getOIDCPublicKey())
            .thenReturn(OIDCPublicKeysResponse())

        Mockito.`when`(oidcHelper.parseKakaoIdToken(Mockito.anyString(), Mockito.anyList()))
            .thenReturn(KakaoIdTokenPayload(
                iss = "",
                aud = "",
                sub = "social-user-id",
                iat = 1L,
                exp = 1L,
                authTime = 1L,
                nickname = "testnick",
            ))

        // given
        val kakaoAccessToken = "test-kakao-id-token"

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