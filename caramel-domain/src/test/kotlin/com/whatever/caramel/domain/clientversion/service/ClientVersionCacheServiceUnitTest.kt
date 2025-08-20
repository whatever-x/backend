package com.whatever.caramel.domain.clientversion.service

import com.whatever.caramel.domain.clientversion.model.OsType
import com.whatever.caramel.domain.clientversion.model.OsVersionPolicy
import com.whatever.caramel.domain.clientversion.repository.ClientVersionRepository
import com.whatever.caramel.domain.clientversion.repository.OsVersionPolicyRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import java.util.Optional
import kotlin.test.Test

class ClientVersionCacheServiceUnitTest : ClientVersionServiceTestSupport {
    private val mockClientVersionRepository = mockk<ClientVersionRepository>()
    private val mockOsVersionPolicyRepository = mockk<OsVersionPolicyRepository>()
    private val clientVersionCacheService = ClientVersionCacheService(
        mockClientVersionRepository,
        mockOsVersionPolicyRepository,
    )

    @DisplayName("최신 버전과 최소 버전이 모두 존재하면, 각 버전을 올바르게 반환한다")
    @Test
    fun getActiveVersions() {
        // given
        val osType = OsType.ANDROID
        val latestVersion = createDummyVersion(major = 10, minor = 0, patch = 0, build = 10)
        val minimumVersion = createDummyVersion(major = 10, minor = 0, patch = 0, build = 9)
        val recommendedVersion = createDummyVersion(major = 10, minor = 0, patch = 0, build = 9)
        val policy = OsVersionPolicy(osType, minimumVersion, recommendedVersion)

        every { mockClientVersionRepository.findLatestVersionByOsType(osType) } returns latestVersion
        every { mockOsVersionPolicyRepository.findById(osType) } returns Optional.of(policy)

        // when
        val result = clientVersionCacheService.getActiveVersions(osType)

        // then
        assertThat(result.latest?.code).isEqualTo(latestVersion.code)
        assertThat(result.minimum?.code).isEqualTo(minimumVersion.code)
        assertThat(result.recommended?.code).isEqualTo(recommendedVersion.code)

        verify(exactly = 1) { mockClientVersionRepository.findLatestVersionByOsType(osType) }
        verify(exactly = 1) { mockOsVersionPolicyRepository.findById(osType) }
    }

    @DisplayName("정책은 존재하지만 권장 버전(recommendedVersion)이 null이면, 결과의 recommended도 null을 반환한다")
    @Test
    fun getActiveVersions_WhenRecommendedVersionIsNullInPolicy() {
        // given
        val osType = OsType.ANDROID
        val latestVersion = createDummyVersion(major = 10, minor = 0, patch = 0, build = 10)
        val minimumVersion = createDummyVersion(major = 10, minor = 0, patch = 0, build = 9)
        val policy = OsVersionPolicy(osType, minimumVersion, null)

        every { mockClientVersionRepository.findLatestVersionByOsType(osType) } returns latestVersion
        every { mockOsVersionPolicyRepository.findById(osType) } returns Optional.of(policy)

        // when
        val result = clientVersionCacheService.getActiveVersions(osType)

        // then
        assertThat(result.latest?.code).isEqualTo(latestVersion.code)
        assertThat(result.minimum?.code).isEqualTo(minimumVersion.code)
        assertThat(result.recommended?.code).isNull()

        verify(exactly = 1) { mockClientVersionRepository.findLatestVersionByOsType(osType) }
        verify(exactly = 1) { mockOsVersionPolicyRepository.findById(osType) }
    }

    @DisplayName("DB에 버전 정보가 없으면, latest와 minimum, recommended 모두 null을 반환한다")
    @Test
    fun getActiveVersions_WhenClientVersionNotExists_ThenReturnNull() {
        // given
        val osType = OsType.ANDROID

        every { mockClientVersionRepository.findLatestVersionByOsType(osType) } returns null
        every { mockOsVersionPolicyRepository.findById(osType) } returns Optional.empty()

        // when
        val result = clientVersionCacheService.getActiveVersions(osType)

        // then
        assertThat(result.latest).isNull()
        assertThat(result.minimum).isNull()
        assertThat(result.recommended).isNull()
    }

    @DisplayName("DB에 정책 정보가 없으면, minimum과 recommended는 null을 반환한다")
    @Test
    fun getActiveVersions_WhenPolicyNotExists() {
        // given
        val osType = OsType.ANDROID
        val latestVersion = createDummyVersion(10, 0, 1, 0)

        every { mockClientVersionRepository.findLatestVersionByOsType(osType) } returns latestVersion
        every { mockOsVersionPolicyRepository.findById(osType) } returns Optional.empty()

        // when
        val result = clientVersionCacheService.getActiveVersions(osType)

        // then
        assertThat(result.latest).isNotNull()
        assertThat(result.minimum).isNull()
        assertThat(result.recommended).isNull()
    }
}
