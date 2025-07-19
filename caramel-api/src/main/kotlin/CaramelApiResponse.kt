package com.whatever

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
com.whatever.caramel.common.global.exception.ErrorUi
import global.exception.common.CaramelExceptionCode

data class CaramelApiResponse<T>(
    val success: Boolean,
    @JsonSerialize(nullsUsing = NullToEmptyObjectSerializer::class)
    val data: T?,
    val error: global.exception.dto.ErrorResponse?,
) {

    companion object {
        fun <T> succeed(data: T? = null): CaramelApiResponse<T> {
            return CaramelApiResponse(
                success = true,
                data = data,
                error = null,
            )
        }

        fun failed(
            code: CaramelExceptionCode,
            errorUi: ErrorUi,
        ): CaramelApiResponse<*> {
            return CaramelApiResponse(
                success = false,
                data = null,
                error = global.exception.dto.ErrorResponse.Companion.of(
                    errorCode = code,
                    errorUi = errorUi,
                )
            )
        }
    }
}

fun <T> T.succeed() = CaramelApiResponse(
    success = true,
    data = this,
    error = null,
)

class NullToEmptyObjectSerializer : StdSerializer<Any?>(Any::class.java as Class<Any?>) {  // Nullable을 한번 더 명시
    override fun serialize(
        value: Any?,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        gen.writeStartObject()
        gen.writeEndObject()
    }
}
