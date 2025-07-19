package com.whatever.domain.auth.dto

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class AppleAuthFormData(
    val code: String,
    id_token: String,  // apple에서 formdata 전송 시 snake-case를 사용
    val state: String? = null,
    private val user: String? = null,
) {
    val idToken = id_token

    val appleUser: AppleUser?
        get() {
            return user?.let { jacksonObjectMapper().readValue(user, AppleUser::class.java) }
        }

    override fun toString(): String {
        val className = this::class.simpleName
        return "${className}(code=${code}, idToken=${idToken}, state=${state}, appleUser=${appleUser})"
    }
}

data class AppleUser(
    val email: String? = null,
    val name: AppleUserName? = null,
)

data class AppleUserName(
    val firstName: String,
    val lastName: String,
) {
    override fun toString(): String {
        return "${lastName}${firstName}"
    }
}
