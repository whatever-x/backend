package com.whatever.caramel.common.util

import java.nio.charset.StandardCharsets
import java.util.Base64

object CursorUtil {
    private const val SEPARATOR: String = "|"

    fun fromHash(cursorHash: String): List<String> {
        val decodedBytes = Base64.getUrlDecoder().decode(cursorHash)
        val decodedString = String(decodedBytes, StandardCharsets.UTF_8)
        return decodedString.split(SEPARATOR)
    }

    fun toHash(vararg cursors: Any): String {
        return cursors.joinToString(SEPARATOR) { it.toString() }
            .toByteArray(StandardCharsets.UTF_8)
            .let { Base64.getUrlEncoder().withoutPadding().encodeToString(it) }
    }
}
