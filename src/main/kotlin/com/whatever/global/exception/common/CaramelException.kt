package com.whatever.global.exception.common

import com.whatever.global.exception.ErrorUiType

/**
 * 커스텀 예외 객체들의 부모
 * @param errorCode 예외 코드를 지정
 * @param detailMessage (선택)디버깅에 필요한 추가적인 메시지
 * @param overrideErrorUiType (선택)errorCode에 정의된 UI Type를 재정의
 */
abstract class CaramelException(
    val errorCode: CaramelExceptionCode,
    val detailMessage: String? = null,
    val overrideErrorUiType: ErrorUiType? = null
) : RuntimeException(errorCode.message)
