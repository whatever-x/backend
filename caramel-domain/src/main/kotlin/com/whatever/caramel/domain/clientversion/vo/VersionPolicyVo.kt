package com.whatever.caramel.domain.clientversion.vo

import com.whatever.caramel.domain.clientversion.model.OsType

sealed interface VersionPolicyVo

data object NoUpdate : VersionPolicyVo
data class ForceUpdate(val latestVersionCode: Int, val osType: OsType) : VersionPolicyVo
data class RecommendUpdate(val latestVersionCode: Int, val osType: OsType) : VersionPolicyVo
