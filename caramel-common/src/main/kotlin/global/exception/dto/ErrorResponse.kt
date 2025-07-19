package global.exception.dto

import com.whatever.global.exception.ErrorUi
import com.whatever.global.exception.common.CaramelExceptionCode

/**
 * 클라이언트가 수신할 예외 정보 객체
 * @param code 예외 코드
 * @param debugMessage ExceptionCode 객체에 있는 message
 * @param message 사용자에게 전달될 에러 title
 * @param description (선택)사용자에게 전달될 에러 description
 * @param errorUiType TOAST | DIALOG
 */
data class ErrorResponse(
    val code: String,
    val debugMessage: String,
    val message: String,
    val description: String?,
    val errorUiType: String,
) {
    companion object {
        fun of(
            errorCode: CaramelExceptionCode,
            errorUi: ErrorUi,
        ): ErrorResponse {
            val (description, type) = when (errorUi) {
                is ErrorUi.Dialog -> errorUi.description to "DIALOG"
                is ErrorUi.Toast -> null to "TOAST"
            }
            return ErrorResponse(
                code = errorCode.code,
                debugMessage = errorCode.message,
                message = errorUi.title,
                description = description,
                errorUiType = type,
            )
        }
    }
}
