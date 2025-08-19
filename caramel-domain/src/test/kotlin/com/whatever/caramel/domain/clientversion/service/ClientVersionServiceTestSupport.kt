package com.whatever.caramel.domain.clientversion.service

import com.whatever.caramel.domain.clientversion.model.ClientVersion
import com.whatever.caramel.domain.clientversion.model.OsType
import com.whatever.caramel.domain.clientversion.vo.ClientVersionVo

interface ClientVersionServiceTestSupport {
    fun createDummyVersionVo(
        major: Int,
        minor: Int,
        patch: Int,
        build: Int,
    ): ClientVersionVo {
        return ClientVersionVo.from(
            createDummyVersion(
                major = major,
                minor = minor,
                patch = patch,
                build = build
            )
        )
    }

    fun createDummyVersion(
        major: Int,
        minor: Int,
        patch: Int,
        build: Int,
    ): ClientVersion {
        return ClientVersion(
            osType = OsType.ANDROID,
            major = major,
            minor = minor,
            patch = patch,
            build = build,
            releaseNote = "test version",
        )
    }
}