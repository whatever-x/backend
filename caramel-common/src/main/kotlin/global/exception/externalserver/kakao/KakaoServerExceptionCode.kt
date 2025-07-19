package global.exception.externalserver.kakao

import global.exception.externalserver.ExternalServerExceptionCode
import org.springframework.http.HttpStatus

enum class KakaoServerExceptionCode(
    val sequence: String,
    val kakaoErrorCode: String,
    override val message: String,
    override val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : ExternalServerExceptionCode {

    // 알 수 없는 오류 (기본값)
    UNKNOWN(
        sequence = "000",
        kakaoErrorCode = 0.toString(),
        message = "알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."
    ),

    // 카카오 공통 에러코드 https://developers.kakao.com/docs/latest/ko/rest-api/reference#error-code-common
    INTERNAL_PROCESSING_ERROR_RETRY(
        sequence = "001",
        kakaoErrorCode = (-1).toString(),
        message = "처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."
    ),
    MISSING_OR_INVALID_PARAMETER(
        sequence = "002",
        kakaoErrorCode = (-2).toString(),
        message = "요청에 필요한 정보가 누락되었거나 잘못되었습니다. 입력 내용을 확인해 주세요."
    ),
    ACCOUNT_SUSPENDED(
        sequence = "003",
        kakaoErrorCode = (-4).toString(),
        message = "계정에 이용 제한이 있습니다. 카카오 고객센터에 문의해 주세요.",
        status = HttpStatus.FORBIDDEN
    ),
    SERVICE_CHECK(
        sequence = "004",
        kakaoErrorCode = (-7).toString(),
        message = "카카오 서버의 서비스 점검 중이거나 내부 문제로 오류가 발생했습니다.",
        status = HttpStatus.BAD_GATEWAY
    ),
    INVALID_HEADER(
        sequence = "005",
        kakaoErrorCode = (-8).toString(),
        message = "잘못된 요청 헤더입니다. 요청 정보를 확인해 주세요."
    ),
    DEPRECATED_API(
        sequence = "006",
        kakaoErrorCode = (-9).toString(),
        message = "더 이상 지원되지 않는 API를 호출하였습니다."
    ),
    QUOTA_EXCEEDED(
        sequence = "007",
        kakaoErrorCode = (-10).toString(),
        message = "요청 횟수를 초과했습니다. 잠시 후 다시 시도해 주세요.",
        status = HttpStatus.TOO_MANY_REQUESTS
    ),
    INVALID_APPKEY_OR_TOKEN(
        sequence = "008",
        kakaoErrorCode = (-401).toString(),
        message = "카카오 인증 정보가 유효하지 않습니다.",
        status = HttpStatus.UNAUTHORIZED
    ),
    KAKAO_TALK_NOT_SIGNED(
        sequence = "009",
        kakaoErrorCode = (-501).toString(),
        message = "카카오톡 가입 이력이 없습니다. 카카오톡 회원가입 후 다시 시도해 주세요."
    ),
    INTERNAL_TIMEOUT(
        sequence = "010",
        kakaoErrorCode = (-603).toString(),
        message = "요청 처리 중 타임아웃이 발생했습니다. 다시 시도해 주세요.",
        status = HttpStatus.GATEWAY_TIMEOUT
    ),
    SERVICE_UNDER_MAINTENANCE(
        sequence = "011",
        kakaoErrorCode = (-9798).toString(),
        message = "서비스가 점검 중입니다. 잠시 후 다시 시도해 주세요.",
        status = HttpStatus.SERVICE_UNAVAILABLE
    ),

    // 카카오 로그인 에러코드 https://developers.kakao.com/docs/latest/ko/rest-api/reference#error-code-kakaologin
    NOT_CONNECTED_KAKAO_ACCOUNT(
        sequence = "012",
        kakaoErrorCode = (-101).toString(),
        message = "카카오 계정이 연결되어 있지 않습니다. 연결 후 다시 시도해 주세요."
    ),
    ALREADY_CONNECTED(
        sequence = "013",
        kakaoErrorCode = (-102).toString(),
        message = "이미 연결된 계정입니다."
    ),
    NON_EXISTENT_OR_DORMANT_ACCOUNT(
        sequence = "014",
        kakaoErrorCode = (-103).toString(),
        message = "존재하지 않거나 휴면 상태인 계정입니다."
    ),
    INVALID_USER_PROPERTY(
        sequence = "015",
        kakaoErrorCode = (-201).toString(),
        message = "요청한 사용자 정보가 올바르지 않습니다."
    ),
    CONSENT_REQUIRED(
        sequence = "016",
        kakaoErrorCode = (-402).toString(),
        message = "추가 동의가 필요한 기능입니다. 동의 후 다시 시도해 주세요.",
        status = HttpStatus.FORBIDDEN
    ),
    UNDERAGE_USER_NOT_ALLOWED(
        sequence = "017",
        kakaoErrorCode = (-406).toString(),
        message = "서비스 이용이 제한된 연령입니다.",
        status = HttpStatus.UNAUTHORIZED
    ),

    // 카카오 OIDC 에러코드 https://developers.kakao.com/docs/latest/ko/kakaologin/trouble-shooting#oidc
    INVALID_OIDC_TOKEN(
        sequence = "018",
        kakaoErrorCode = "KOE400",
        message = "카카오 인증 토큰이 없거나, 올바른 형식이 아닙니다."
    ),
    INVALID_OIDC_ISS(
        sequence = "019",
        kakaoErrorCode = "KOE401",
        message = "올바른 카카오 ID 토큰이 아닙니다."
    ),
    INVALID_OIDC_SIGNATURE(
        sequence = "020",
        kakaoErrorCode = "KOE402",
        message = "올바른 카카오 ID 토큰이 아닙니다."
    ),
    EXPIRED_OIDC_TOKEN(
        sequence = "021",
        kakaoErrorCode = "KOE403",
        message = "카카오 ID 토큰이 만료되었습니다.",
        status = HttpStatus.UNAUTHORIZED
    ),
    ;

    override val code: String
        get() = "KAKAO$sequence"

    companion object {
        fun fromKakaoErrorCode(kakaoErrorCode: Int): KakaoServerExceptionCode {
            return entries.firstOrNull { it.kakaoErrorCode == kakaoErrorCode.toString() } ?: UNKNOWN
        }

        fun fromKakaoErrorCode(kakaoErrorCode: String?): KakaoServerExceptionCode {
            return entries.firstOrNull { it.kakaoErrorCode == kakaoErrorCode } ?: UNKNOWN
        }
    }
}
