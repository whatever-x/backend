package com.whatever.caramel.domain.clientversion.service

import com.whatever.caramel.domain.CaramelDomainSpringBootTest
import com.whatever.caramel.domain.clientversion.model.OsType
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
import kotlin.test.Test

@CaramelDomainSpringBootTest
class ClientVersionCacheServiceTest @Autowired constructor(
    private val redisConnectionFactory: RedisConnectionFactory,
    private val clientVersionCacheService: ClientVersionCacheService,
) : ClientVersionServiceTestSupport {

    @MockitoBean
    private lateinit var clientVersionRepository: ClientVersionRepository

    @AfterEach
    fun tearDown() {
        redisConnectionFactory.connection.serverCommands().flushAll()
    }

    @DisplayName("한번 호출한 뒤에는 캐싱이 적용되어 DB를 조회하지 않는다.")
    @Test
    fun getActiveVersions_WithCache() {
        // given
        val osType = OsType.ANDROID
        val latestVersion = createDummyVersion(
            isMinimum = false,
            major = 10,
            minor = 0,
            patch = 0,
            build = 10,
        )
        val minimumVersion = createDummyVersion(
            isMinimum = true,
            major = 10,
            minor = 0,
            patch = 0,
            build = 9,
        )
        whenever(clientVersionRepository.findLatestVersionByOsType(osType)).thenReturn(latestVersion)
        whenever(clientVersionRepository.findMinimumVersionByOsType(osType)).thenReturn(minimumVersion)

        // when
        val firstResult = clientVersionCacheService.getActiveVersions(osType)
        val secondResult = clientVersionCacheService.getActiveVersions(osType)

        // then
        assertThat(firstResult).isEqualTo(secondResult)
        assertThat(secondResult.latest?.code).isEqualTo(latestVersion.code)
        assertThat(secondResult.minimum?.code).isEqualTo(minimumVersion.code)

        verify(clientVersionRepository, times(1)).findLatestVersionByOsType(osType)
        verify(clientVersionRepository, times(1)).findMinimumVersionByOsType(osType)
    }
}