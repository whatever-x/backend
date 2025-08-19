package com.whatever.caramel.domain.clientversion.service

import com.whatever.caramel.domain.clientversion.model.OsType
import com.whatever.caramel.domain.clientversion.vo.ForceUpdate
import com.whatever.caramel.domain.clientversion.vo.NoUpdate
import com.whatever.caramel.domain.clientversion.vo.RecommendUpdate
import com.whatever.caramel.domain.clientversion.vo.SupportedVersionsVo
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test

class ClientVersionServiceUnitTest : ClientVersionServiceTestSupport {
    private val mockClientVersionCacheService = mockk<ClientVersionCacheService>()
    private val clientVersionService = ClientVersionService(mockClientVersionCacheService)

    @DisplayName("사용자 버전 경계이 최소 버전 경계보다 낮으면, ForceUpdate를 반환한다")
    @Test
    fun checkVersion_WhenLowerThanMinimumVersion_ThenReturnForceUpdate() {
        // given
        val osType = OsType.ANDROID
        val latestVo = createDummyVersionVo(major = 10, minor = 0, patch = 0, build = 10)
        val recommendedVo = createDummyVersionVo(major = 10, minor = 0, patch = 0, build = 9)
        val minimumVo = createDummyVersionVo(major = 10, minor = 0, patch = 0, build = 8)
        val supportedVersions = SupportedVersionsVo(latest = latestVo, recommended = recommendedVo, minimum = minimumVo)

        every { mockClientVersionCacheService.getActiveVersions(osType) } returns supportedVersions

        val userVersionCode = minimumVo.code - 1  // 최소 지원 버전 경계보다 낮은 버전 경계

        // when
        val result = clientVersionService.checkVersion(osType, userVersionCode)

        // then
        assertThat(result).isInstanceOf(ForceUpdate::class.java)
        assertThat((result as ForceUpdate).latestVersionCode).isEqualTo(latestVo.code)
    }

    @DisplayName("사용자 버전 경계이 최소 버전 경계보다는 높고 권장 버전 경계보다는 낮으면, RecommendUpdate를 반환한다")
    @Test
    fun checkVersion_WhenBetweenMinimumAndLatest_ThenReturnRecommendUpdate() {
        // given
        val osType = OsType.ANDROID
        val latestVo = createDummyVersionVo(major = 10, minor = 0, patch = 0, build = 11)
        val recommendedVo = createDummyVersionVo(major = 10, minor = 0, patch = 0, build = 10)
        val minimumVo = createDummyVersionVo(major = 10, minor = 0, patch = 0, build = 8)

       val clientVersionCode = (minimumVo.code + recommendedVo.code) shr 1

        val supportedVersions = SupportedVersionsVo(latest = latestVo, recommended = recommendedVo, minimum = minimumVo)
        every { mockClientVersionCacheService.getActiveVersions(osType) } returns supportedVersions


        // when
        val result = clientVersionService.checkVersion(osType, clientVersionCode)

        // then
        assertThat(result).isInstanceOf(RecommendUpdate::class.java)
        assertThat((result as RecommendUpdate).latestVersionCode).isEqualTo(latestVo.code)
    }

    @DisplayName("사용자 버전 경계이 최신 버전 경계과 같으면, NoUpdate를 반환한다")
    @Test
    fun checkVersion_WhenEqualLatestVersion_ThenReturnNoUpdate() {
        // given
        val osType = OsType.ANDROID
        val latestVo = createDummyVersionVo(major = 10, minor = 0, patch = 0, build = 10)
        val recommendedVo = createDummyVersionVo(major = 10, minor = 0, patch = 0, build = 9)
        val minimumVo = createDummyVersionVo(major = 10, minor = 0, patch = 0, build = 8)

        val supportedVersions = SupportedVersionsVo(latest = latestVo, recommended = recommendedVo, minimum = minimumVo)

        every { mockClientVersionCacheService.getActiveVersions(osType) } returns supportedVersions

        val userVersionCode = latestVo.code // 최신 버전 경계과 동일

        // when
        val result = clientVersionService.checkVersion(osType, userVersionCode)

        // then
        assertThat(result).isEqualTo(NoUpdate)
    }

    @DisplayName("사용자 버전 경계이 최신 버전 경계보다 높으면, NoUpdate를 반환한다")
    @Test
    fun checkVersion_WhenHigherThanLatestVersion_ThenReturnNoUpdate() {
        // given
        val osType = OsType.ANDROID
        val latestVo = createDummyVersionVo(major = 10, minor = 0, patch = 0, build = 10)
        val recommendedVo = createDummyVersionVo(major = 10, minor = 0, patch = 0, build = 9)
        val minimumVo = createDummyVersionVo(major = 10, minor = 0, patch = 0, build = 8)
        val supportedVersions = SupportedVersionsVo(latest = latestVo, recommended = recommendedVo, minimum = minimumVo)

        every { mockClientVersionCacheService.getActiveVersions(osType) } returns supportedVersions

        val userVersionCode = latestVo.code + 1 // 최신 버전 경계보다 높은 버전 경계

        // when
        val result = clientVersionService.checkVersion(osType, userVersionCode)

        // then
        assertThat(result).isEqualTo(NoUpdate)
    }

    @DisplayName("최신 버전 경계 정보가 없으면, NoUpdate를 반환한다")
    @Test
    fun checkVersion_WhenLatestVersionNotExists_ThenReturnNoUpdate() {
        // given
        val osType = OsType.ANDROID
        val versionCode = 10000000

        val supportedVersions = SupportedVersionsVo(latest = null, recommended = null, minimum = null)
        every { mockClientVersionCacheService.getActiveVersions(osType) } returns supportedVersions

        // when
        val result = clientVersionService.checkVersion(osType, versionCode)

        // then
        assertThat(result).isEqualTo(NoUpdate)
    }

    @DisplayName("최소 지원 버전 경계 정보가 없으면, NoUpdate를 반환한다")
    @Test
    fun checkVersion_WhenMinimumVersionNotExists_ThenReturnNoUpdate() {
        // given
        val osType = OsType.ANDROID
        val latestVo = createDummyVersionVo(major = 10, minor = 0, patch = 0, build = 10)
        val supportedVersions = SupportedVersionsVo(latest = latestVo, recommended = null, minimum = null)
        every { mockClientVersionCacheService.getActiveVersions(osType) } returns supportedVersions

        // when
        val result = clientVersionService.checkVersion(osType, latestVo.code)

        // then
        assertThat(result).isEqualTo(NoUpdate)
    }

    @DisplayName("권장 버전이 설정되지 않았고 사용자 버전이 최소 버전 이상이면, NoUpdate를 반환한다")
    @Test
    fun checkVersion_WhenRecommendedIsNull_ThenReturnNoUpdate() {
        // given
        val osType = OsType.ANDROID
        val latestVo = createDummyVersionVo(10, 0, 0, 10)
        val minimumVo = createDummyVersionVo(10, 0, 0, 8)

        val supportedVersions = SupportedVersionsVo(latest = latestVo,  recommended = null, minimum = minimumVo)
        every { mockClientVersionCacheService.getActiveVersions(osType) } returns supportedVersions

        val userVersionCode = minimumVo.code + 1

        // when
        val result = clientVersionService.checkVersion(osType, userVersionCode)

        // then
        assertThat(result).isEqualTo(NoUpdate)
    }
}
