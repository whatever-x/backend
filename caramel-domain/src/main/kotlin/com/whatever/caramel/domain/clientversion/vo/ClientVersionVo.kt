package com.whatever.caramel.domain.clientversion.vo

import com.whatever.caramel.domain.clientversion.model.ClientVersion
import com.whatever.caramel.domain.clientversion.model.OsType

data class SupportedVersionsVo(
    val latest: ClientVersionVo?,
    val minimum: ClientVersionVo?,
)

data class ClientVersionVo(
    val osType: OsType,
    val major: Int,
    val minor: Int,
    val patch: Int,
    val build: Int,
    val code: Int,
    val isMinimum: Boolean,
    val releaseNote: String?,
){
    companion object {
        fun from(clientVersion: ClientVersion): ClientVersionVo {
            return ClientVersionVo(
                osType = clientVersion.osType,
                major = clientVersion.major,
                minor = clientVersion.minor,
                patch = clientVersion.patch,
                build = clientVersion.build,
                code = clientVersion.code,
                isMinimum = clientVersion.isMinimum,
                releaseNote = clientVersion.releaseNote
            )
        }
    }
}
