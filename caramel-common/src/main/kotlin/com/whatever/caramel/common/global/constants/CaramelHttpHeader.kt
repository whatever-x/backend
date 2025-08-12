package com.whatever.caramel.common.global.constants

object CaramelHttpHeaders {
    const val TIME_ZONE = "Time-Zone"
    const val AUTH_JWT_HEADER = "Authorization"
    const val DEVICE_ID = "Device-Id"
    const val OS_TYPE = "Os-Type"

    val ALL_HEADERS = setOf(
        TIME_ZONE,
        AUTH_JWT_HEADER,
        DEVICE_ID,
        OS_TYPE,
    )
}
