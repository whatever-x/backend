package com.whatever.caramel.domain.clientversion.service

import com.whatever.caramel.domain.clientversion.model.OsType
import com.whatever.caramel.domain.clientversion.vo.ForceUpdate
import com.whatever.caramel.domain.clientversion.vo.NoUpdate
import com.whatever.caramel.domain.clientversion.vo.RecommendUpdate
import com.whatever.caramel.domain.clientversion.vo.VersionPolicyVo
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger { }

@Service
class ClientVersionService(
    private val clientVersionCacheService: ClientVersionCacheService,
) {
    fun checkVersion(osType: OsType, versionCode: Int): VersionPolicyVo {

        val (latestVersion, minimumVersion) = clientVersionCacheService.getActiveVersions(osType)
        if (latestVersion == null || minimumVersion == null) {
            logger.warn { "Failed to find valid client version(Latest or Minimum) for osType: ${osType}" }
            return NoUpdate
        }

        return when {
            versionCode < minimumVersion.code -> ForceUpdate(
                latestVersionCode = latestVersion.code,
                osType = osType
            )

            versionCode < latestVersion.code -> RecommendUpdate(
                latestVersionCode = latestVersion.code,
                osType = osType,
            )

            else -> NoUpdate
        }
    }
}
