package com.whatever.caramel.security.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.whatever.caramel.common.global.exception.ErrorUi
import com.whatever.caramel.common.global.exception.common.CaramelExceptionCode
import com.whatever.caramel.common.response.CaramelApiResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType

fun HttpServletResponse.setExceptionResponse(
    errorCode: CaramelExceptionCode,
    errorUi: ErrorUi,
    objectMapper: ObjectMapper,
) {
    characterEncoding = Charsets.UTF_8.name()
    contentType = MediaType.APPLICATION_JSON_VALUE
    status = errorCode.status.value()
    writer.write(
        objectMapper.writeValueAsString(
            CaramelApiResponse.failed(
                code = errorCode,
                errorUi = errorUi,
            )
        )
    )
    writer.flush()
    writer.close()
}
