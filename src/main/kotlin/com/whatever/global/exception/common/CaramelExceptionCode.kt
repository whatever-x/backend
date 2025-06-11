package com.whatever.global.exception.common

import com.whatever.global.exception.ErrorUiType
import org.springframework.http.HttpStatus

interface CaramelExceptionCode {
    val status: HttpStatus
    val code: String
    val message: String
    val description: String?
    val errorUiType: ErrorUiType
}
