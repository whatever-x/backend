package com.whatever.caramel.domain.user.vo

import com.whatever.caramel.domain.user.model.UserGender
import java.time.LocalDate

data class CreateUserProfileVo(
    val nickname: String,
    val birthday: LocalDate,
    val gender: UserGender,
    val agreementServiceTerms: Boolean,
    val agreementPrivatePolicy: Boolean,
) 
