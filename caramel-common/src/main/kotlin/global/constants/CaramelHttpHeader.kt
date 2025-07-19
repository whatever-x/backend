package global.constants

object CaramelHttpHeaders {
    const val TIME_ZONE = "Time-Zone"
    const val AUTH_JWT_HEADER = "Authorization"
    const val DEVICE_ID = "Device-Id"

    val ALL_HEADERS = setOf(
        TIME_ZONE,
        AUTH_JWT_HEADER,
        DEVICE_ID,
    )
}
