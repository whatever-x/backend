package com.whatever.global.jwt.exception

import com.whatever.global.exception.ErrorUiType
import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class JwtExceptionCode(
    sequence: String,
    override val message: String,
    override val description: String? = null,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
    override val errorUiType: ErrorUiType = ErrorUiType.TOAST,
) : CaramelExceptionCode {

    UNKNOWN(
        sequence = "000",
        message = "JWT를 파싱하거나 검증하는 과정에서 예상치 못한 오류가 발생했습니다."
    ),
    MALFORMED(
        sequence = "001",
        message = "JWT 형식이 잘못되었습니다."
    ),
    SIGNATURE_INVALID(
        sequence = "002",
        message = "JWT 서명 검증에 실패했습니다.",
        status = HttpStatus.UNAUTHORIZED
    ),
    SECURITY_FAILURE(
        sequence = "003",
        message = "JWT 암호 해독에 실패했습니다.",
        status = HttpStatus.UNAUTHORIZED
    ),
    EXPIRED(
        sequence = "004",
        message = "JWT가 만료되었습니다.",
        status = HttpStatus.UNAUTHORIZED
    ),
    UNSUPPORTED(
        sequence = "005",
        message = "지원되지 않는 JWT 형식입니다."
    ),
    MISSING_CLAIM(
        sequence = "006",
        message = "JWT에 필요한 정보가 없습니다."
    ),
    PARSE_FAILED(
        sequence = "007",
        message = "인증 토큰이 유효하지 않습니다."
    ),
    MISSING_JTI(
        sequence = "008",
        message = "JWT에 필요한 id 정보가 없습니다."
    ),
    MISSING_EXP_DATE(
        sequence = "009",
        message = "JWT에 필요한 만료 정보가 없습니다."
    ),
    ;

    override val code = "JWT$sequence"
}
