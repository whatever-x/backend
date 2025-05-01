package com.whatever.util

import java.util.*

object CursorUtil {
    private const val SEPARATOR: String = "|"

    fun fromHash(cursorHash: String): List<String> {
        return Base64.getUrlDecoder().decode(cursorHash).toString().split(SEPARATOR)
    }

    fun toHash(vararg cursors: Any): String {
        return cursors.joinToString(SEPARATOR) { it.toString() }
            .let {
                Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(it.toByteArray())
            }

    }
}