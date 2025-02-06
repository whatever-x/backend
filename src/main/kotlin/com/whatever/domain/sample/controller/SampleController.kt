package com.whatever.domain.sample.controller

import com.whatever.domain.sample.exception.SampleExceptionCode
import com.whatever.domain.sample.exception.SampleNotFoundException
import com.whatever.domain.sample.service.SampleService
import com.whatever.global.exception.dto.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.Controller

@Tag(
    name = "Sample Controller",
    description = "간단한 예제를 담은 샘플 API 입니다."
)
@RestController
class SampleController(
    private val sampleService: SampleService,
) {

    @Operation(
        summary = "기능명 기재",
        description = "기능 설명 기재",
        responses = [
            ApiResponse(responseCode = "200", description = "응답에 대한 설명"),
            ApiResponse(responseCode = "400", description = "응답에 대한 설명2"),
        ]
    )
    @GetMapping("/sample")
    fun getSample(): ResponseEntity<String> {
        val result: String = sampleService.getSample()
        return ResponseEntity.ok(result)
    }

    @Operation(
        summary = "기능명 기재",
        description = "기능 설명 기재",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "응답에 대한 설명"),
        ApiResponse(responseCode = "400", description = "응답에 대한 설명2"),
        ApiResponse(
            responseCode = "404",
            description = "응답에 대한 설명3",
            content = [  // 해당 응답에 대한 스키마 추가 가능
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    //array = ArraySchema(schema = Schema(implementation = ExceptionResponse::class))  // array도 가능
                )
            ]
        )
    )
    @GetMapping("/exception")
    fun getException(): ResponseEntity<String> {
        throw SampleNotFoundException(SampleExceptionCode.SAMPLE_CODE, "예외 생성 예시입니다.")
        return ResponseEntity.ok("exception not found")
    }
}