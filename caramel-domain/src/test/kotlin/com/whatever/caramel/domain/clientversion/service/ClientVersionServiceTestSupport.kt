package com.whatever.caramel.domain.clientversion.service

import com.whatever.caramel.domain.clientversion.model.ClientVersion
import com.whatever.caramel.domain.clientversion.model.OsType

interface ClientVersionServiceTestSupport {
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