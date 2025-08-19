package com.whatever.caramel.domain.clientversion.service

import com.whatever.caramel.domain.CaramelDomainSpringBootTest
import com.whatever.caramel.domain.clientversion.model.OsVersionPolicy
import com.whatever.caramel.domain.clientversion.model.OsType
import com.whatever.caramel.domain.clientversion.repository.OsVersionPolicyRepository
import com.whatever.caramel.domain.clientversion.repository.ClientVersionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.Optional
import kotlin.test.Test

@CaramelDomainSpringBootTest
class ClientVersionCacheServiceTest @Autowired constructor(
    private val redisConnectionFactory: RedisConnectionFactory,
    private val clientVersionCacheService: ClientVersionCacheService,
) : ClientVersionServiceTestSupport {

    @MockitoBean
    private lateinit var clientVersionRepository: ClientVersionRepository

    @MockitoBean
    private lateinit var osVersionPolicyRepository: OsVersionPolicyRepository

    @AfterEach
    fun tearDown() {
        redisConnectionFactory.connection.serverCommands().flushAll()
    }

    @DisplayName("한번 호출한 뒤에는 캐싱이 적용되어 DB를 조회하지 않는다.")
    @Test
    fun getActiveVersions_WithCache() {
        // given
        val osType = OsType.ANDROID
        val latestVersion = createDummyVersion(major = 10, minor = 0, patch = 0, build = 10)
        val minimumVersion = createDummyVersion(major = 10, minor = 0, patch = 0, build = 9)
        val recommendedVersion = createDummyVersion(major = 10, minor = 0, patch = 0, build = 9)
        val policy = OsVersionPolicy(
            osType = osType,
            minimumVersion = minimumVersion,
            recommendedVersion = recommendedVersion
        )

        whenever(clientVersionRepository.findLatestVersionByOsType(osType)).thenReturn(latestVersion)

        // findByIdOrNul은 Kotlin 확장함수이므로, CrudRepository의 findById를 Stubbing
        whenever(osVersionPolicyRepository.findById(osType)).thenReturn(Optional.of(policy))

        // when
        val firstResult = clientVersionCacheService.getActiveVersions(osType)
        val secondResult = clientVersionCacheService.getActiveVersions(osType)

        // then
        assertThat(firstResult).isEqualTo(secondResult)
        assertThat(secondResult.latest?.code).isEqualTo(latestVersion.code)
        assertThat(secondResult.minimum?.code).isEqualTo(minimumVersion.code)
        assertThat(secondResult.recommended?.code).isEqualTo(recommendedVersion.code)

        verify(clientVersionRepository, times(1)).findLatestVersionByOsType(osType)
        verify(osVersionPolicyRepository, times(1)).findById(osType)
    }
}