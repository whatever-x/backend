package com.whatever.domain.sample.controller

import com.whatever.domain.sample.exception.SampleExceptionCode
import com.whatever.domain.sample.exception.SampleNotFoundException
import com.whatever.domain.sample.service.SampleGetResultDto
import com.whatever.domain.sample.service.SamplePostResultDto
import com.whatever.domain.sample.service.SampleService
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(
    name = "Sample Controller",
    description = "간단한 예제를 담은 샘플 API 입니다."
)
@RestController
@RequestMapping("/sample")
class SampleController(
    private val sampleService: SampleService,
) {

    @Operation(
        summary = "GET 임시 API",
        description = "테스트를 위한 임시 GET API입니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "성공시 다양한 타입에 대한 Json을 전송합니다."),
        ]
    )
    @GetMapping
    fun getSample(): ResponseEntity<CaramelApiResponse<SampleGetResultDto>> {
        val result = sampleService.getSample()
        val apiResult = CaramelApiResponse.succeed(result)
        return ResponseEntity.ok(apiResult)
    }

    @Operation(
        summary = "POST 임시 API",
        description = "테스트를 위한 임시 POST API입니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "성공시 입력에 대해 동적인 반환값을 전달합니다."),
        ]
    )
    @PostMapping
    fun postSample(@RequestBody request: SampleRequestDto): ResponseEntity<CaramelApiResponse<SamplePostResultDto>> {
        val result = sampleService.postSample(request)
        val apiResult = CaramelApiResponse.succeed(result)
        return ResponseEntity.ok(apiResult)
    }

    @Operation(
        summary = "기능명 기재",
        description = "기능 설명 기재",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "400",
            description = "400 입력시 발생하는 예외입니다.",
            content = [Content(schema = Schema(implementation = CaramelApiResponse::class))]
        ),
        ApiResponse(
            responseCode = "401",
            description = "401 입력시 발생하는 예외입니다.",
            content = [Content(schema = Schema(implementation = CaramelApiResponse::class))]
        ),
        ApiResponse(
            responseCode = "403",
            description = "403 입력시 발생하는 예외입니다.",
            content = [Content(schema = Schema(implementation = CaramelApiResponse::class))]
        ),
        ApiResponse(
            responseCode = "500",
            description = "500 입력시 발생하는 예외입니다.",
            content = [Content(schema = Schema(implementation = CaramelApiResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "샘플로 제공하지 않는 예외번호 입력 시 발생하는 예외입니다.",
            content = [Content(schema = Schema(implementation = CaramelApiResponse::class))]
        )
    )
    @GetMapping("/exception/{exceptionNumber}")
    fun getException(@PathVariable("exceptionNumber") exceptionNumber: Int): ResponseEntity<Unit> {
        sampleService.getException(exceptionNumber)
        return ResponseEntity.noContent().build()
    }
}

data class SampleRequestDto(
    val name: String,
)