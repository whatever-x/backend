package com.whatever.caramel.common.global.exception.common

import com.whatever.caramel.common.global.exception.ErrorUi

/**
 * 커스텀 예외 객체들의 부모
 * @param errorCode 예외 코드를 지정
 * @param errorUi 클라이언트에서 예외를 표시할 UI Type
 */
abstract class CaramelException(
    val errorCode: CaramelExceptionCode,
    val errorUi: ErrorUi = ErrorUi.Toast("알 수 없는 에러입니다."),
) : RuntimeException(errorCode.message)
