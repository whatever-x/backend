package com.whatever.caramel.domain.clientversion.service

import com.whatever.caramel.domain.clientversion.model.OsType
import com.whatever.caramel.domain.clientversion.repository.ClientVersionRepository
import com.whatever.caramel.domain.clientversion.vo.SupportedVersionsVo
import com.whatever.caramel.domain.clientversion.vo.ClientVersionVo
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class ClientVersionCacheService(
    private val clientVersionRepository: ClientVersionRepository,
) {
    @Cacheable(
        cacheNames = ["client-versions"],
        key = "#osType",
        unless = "#result.latest == null",
    )
    fun getActiveVersions(osType: OsType): SupportedVersionsVo {
        val latestVersion = clientVersionRepository.findLatestVersionByOsType(osType)
        val minimumVersion = latestVersion?.let { latestVersion ->
            if (latestVersion.isMinimum) latestVersion
            else clientVersionRepository.findMinimumVersionByOsType(osType)
        }

        return SupportedVersionsVo(
            latest = latestVersion?.let { ClientVersionVo.from(latestVersion) },
            minimum = minimumVersion?.let { ClientVersionVo.from(minimumVersion) },
        )
    }
}
