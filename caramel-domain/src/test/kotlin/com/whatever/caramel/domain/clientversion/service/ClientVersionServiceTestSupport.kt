package com.whatever.caramel.domain.clientversion.service

import com.whatever.caramel.domain.clientversion.model.ClientVersion
import com.whatever.caramel.domain.clientversion.model.OsType
import com.whatever.caramel.domain.clientversion.vo.ClientVersionVo

interface ClientVersionServiceTestSupport {
    fun createDummyVersionVo(
        isMinimum: Boolean,
        major: Int,
        minor: Int,
        patch: Int,
        build: Int,
    ): ClientVersionVo {
        return ClientVersionVo.from(
            createDummyVersion(
                isMinimum = isMinimum,
                major = major,
                minor = minor,
                patch = patch,
                build = build
            )
        )
    }

    fun createDummyVersion(
        isMinimum: Boolean,
        major: Int,
        minor: Int,
        patch: Int,
        build: Int,
    ): ClientVersion {
        return ClientVersion(
            osType = OsType.ANDROID,
            isMinimum = isMinimum,
            major = major,
            minor = minor,
            patch = patch,
            build = build,
            releaseNote = "test version",
        )
    }
}