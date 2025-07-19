package global.exception.common

import org.springframework.http.HttpStatus

interface CaramelExceptionCode {
    val status: HttpStatus
    val code: String
    val message: String
}
