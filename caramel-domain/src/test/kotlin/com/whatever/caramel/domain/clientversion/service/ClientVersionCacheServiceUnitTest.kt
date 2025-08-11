package com.whatever.caramel.domain.clientversion.service

import com.whatever.caramel.domain.clientversion.model.OsType
import com.whatever.caramel.domain.clientversion.repository.ClientVersionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test

class ClientVersionCacheServiceUnitTest : ClientVersionServiceTestSupport {
    private val mockClientVersionRepository = mockk<ClientVersionRepository>()
    private val clientVersionCacheService = ClientVersionCacheService(mockClientVersionRepository)

    @DisplayName("최신 버전과 최소 버전이 모두 존재하면, 각 버전을 올바르게 반환한다")
    @Test
    fun getActiveVersions() {
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

        every { mockClientVersionRepository.findLatestVersionByOsType(osType) } returns latestVersion
        every { mockClientVersionRepository.findMinimumVersionByOsType(osType) } returns minimumVersion

        // when
        val result = clientVersionCacheService.getActiveVersions(osType)

        // then
        assertThat(result.latest?.code).isEqualTo(latestVersion.code)
        assertThat(result.minimum?.code).isEqualTo(minimumVersion.code)

        verify(exactly = 1) { mockClientVersionRepository.findLatestVersionByOsType(osType) }
        verify(exactly = 1) { mockClientVersionRepository.findMinimumVersionByOsType(osType) }
    }

    @DisplayName("최신 버전이 최소 버전을 겸하면, findMinimumVersionByOsType은 호출되지 않는다")
    @Test
    fun getActiveVersions_WithLatestAndMinimumClientVersion() {
        // given
        val osType = OsType.ANDROID
        val latestAndMinimumVersion = createDummyVersion(
            isMinimum = true,
            major = 10,
            minor = 0,
            patch = 0,
            build = 10,
        )

        every { mockClientVersionRepository.findLatestVersionByOsType(osType) } returns latestAndMinimumVersion

        // when
        val result = clientVersionCacheService.getActiveVersions(osType)

        // then
        assertThat(result.latest?.code).isEqualTo(latestAndMinimumVersion.code)
        assertThat(result.minimum?.code).isEqualTo(latestAndMinimumVersion.code)

        verify(exactly = 0) { mockClientVersionRepository.findMinimumVersionByOsType(any()) }
    }

    @DisplayName("DB에 버전 정보가 없으면, latest와 minimum 모두 null을 반환한다")
    @Test
    fun getActiveVersions_WhenClientVersionNotExists_ThenReturnNull() {
        // given
        val osType = OsType.ANDROID
        every { mockClientVersionRepository.findLatestVersionByOsType(osType) } returns null
        every { mockClientVersionRepository.findMinimumVersionByOsType(osType) } returns null

        // when
        val result = clientVersionCacheService.getActiveVersions(osType)

        // then
        assertThat(result.latest).isNull()
        assertThat(result.minimum).isNull()

        verify(exactly = 0) { mockClientVersionRepository.findMinimumVersionByOsType(any()) }
    }
}