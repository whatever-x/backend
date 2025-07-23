package com.whatever.caramel.domain.user.vo

import java.time.LocalDate

data class UpdateUserProfileVo(
    val nickname: String?,
    val birthday: LocalDate?,
) 
