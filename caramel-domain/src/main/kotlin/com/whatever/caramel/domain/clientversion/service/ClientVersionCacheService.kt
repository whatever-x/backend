package com.whatever.caramel.domain.clientversion.service

import com.whatever.caramel.domain.clientversion.model.OsType
import com.whatever.caramel.domain.clientversion.repository.OsVersionPolicyRepository
import com.whatever.caramel.domain.clientversion.repository.ClientVersionRepository
import com.whatever.caramel.domain.clientversion.vo.SupportedVersionsVo
import com.whatever.caramel.domain.clientversion.vo.ClientVersionVo
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ClientVersionCacheService(
    private val clientVersionRepository: ClientVersionRepository,
    private val osVersionPolicyRepository: OsVersionPolicyRepository,
) {
    @Cacheable(
        cacheNames = ["app:client-versions"],
        key = "#osType",
        unless = "#result.latest == null",
    )
    fun getActiveVersions(osType: OsType): SupportedVersionsVo {
        val latestVersion = clientVersionRepository.findLatestVersionByOsType(osType)
        val policy = osVersionPolicyRepository.findByIdOrNull(osType)

        return SupportedVersionsVo(
            latest = latestVersion?.let { ClientVersionVo.from(latestVersion) },
            recommended = policy?.recommendedVersion?.let { ClientVersionVo.from(it) },
            minimum = policy?.let { ClientVersionVo.from(it.minimumVersion) },
        )
    }
}
