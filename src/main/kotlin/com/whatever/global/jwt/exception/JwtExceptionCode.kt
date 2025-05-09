package com.whatever.global.jwt.exception

import com.whatever.global.exception.common.CaramelExceptionCode
import org.springframework.http.HttpStatus

enum class JwtExceptionCode(
    sequence: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : CaramelExceptionCode {

    UNKNOWN("000", "JWT를 파싱하거나 검증하는 과정에서 예상치 못한 오류가 발생했습니다."),
    MALFORMED("001", "JWT 형식이 잘못되었습니다."),
    SIGNATURE_INVALID("002", "JWT 서명 검증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    SECURITY_FAILURE("003", "JWT 암호 해독에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED("004", "JWT가 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    UNSUPPORTED("005", "지원되지 않는 JWT 형식입니다."),
    MISSING_CLAIM("006", "JWT에 필요한 정보가 없습니다."),
    PARSE_FAILED("007", "인증 토큰이 유효하지 않습니다."),
    MISSING_JTI("008", "JWT에 필요한 id 정보가 없습니다."),
    ;

    override val code = "JWT$sequence"
}
