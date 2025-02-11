package com.whatever.global.security.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.whatever.global.exception.common.CaramelExceptionCode
import com.whatever.global.exception.dto.CaramelApiResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType

fun HttpServletResponse.setExceptionResponse(
    errorCode: CaramelExceptionCode,
    detailMessage: String? = null,
    objectMapper: ObjectMapper,
) {
    characterEncoding = Charsets.UTF_8.name()
    contentType = MediaType.APPLICATION_JSON_VALUE
    status = errorCode.status.value()
    writer.write(
        objectMapper.writeValueAsString(
            CaramelApiResponse.failed(
                code = errorCode.code,
                message = errorCode.message,
                debugMessage = detailMessage
            )
        )
    )
    writer.flush()
    writer.close()
}
