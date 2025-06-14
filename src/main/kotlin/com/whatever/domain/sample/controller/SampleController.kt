package com.whatever.domain.sample.controller

import com.whatever.domain.auth.dto.SignInResponse
import com.whatever.domain.sample.controller.dto.SampleSendFcmRequest
import com.whatever.domain.sample.exception.SampleExceptionCode
import com.whatever.domain.sample.exception.SampleNotFoundException
import com.whatever.domain.sample.service.SampleService
import com.whatever.domain.user.model.UserGender
import com.whatever.global.annotation.DisableSwaggerAuthButton
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.ErrorResponse
import com.whatever.global.exception.dto.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Profile("dev", "local-mem")
@Tag(
    name = "Sample Controller",
    description = "간단한 예제를 담은 샘플 API 입니다."
)
@RestController
@RequestMapping("/sample")
class SampleController(
    private val sampleService: SampleService,
) {

    @DisableSwaggerAuthButton
    @Operation(
        summary = "fcm 요청을 테스트합니다.",
        description = "fcm notification 전송을 테스트합니다. 타겟 유저에 대한 fcm token 등록이 선행되어야 합니다.",
    )
    @PostMapping("/test/fcm")
    fun sendTestFcm(
        @RequestBody request: SampleSendFcmRequest,
    ) {
        sampleService.sendTestFcmNotification(
            request.targetUserIds,
            request.title,
            request.body,
        )
    }

    @DisableSwaggerAuthButton
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

    @DisableSwaggerAuthButton
    @Operation(
        summary = "개발용 테스트 유저 생성",
        description = "개발용 테스트 유저입니다."
    )
    @GetMapping("/test/sign-up/new")
    fun testSignUpNew(
        @RequestParam(required = false) testEmail: String? = null,
    ): CaramelApiResponse<String> {
        val email = sampleService.createNewDummyAccount(testEmail)
        return email.succeed()
    }

    @DisableSwaggerAuthButton
    @Operation(
        summary = "개발용 테스트 유저 생성",
        description = "개발용 테스트 유저입니다."
    )
    @GetMapping("/test/sign-up/single")
    fun testSignUpSingle(
        @RequestParam(required = false) testEmail: String? = null,
        @RequestParam(required = false) testNickname: String? = null,
        @RequestParam(required = false) testBirthDate: LocalDate? = null,
        @RequestParam(required = false) testGender: UserGender? = null,
    ): CaramelApiResponse<String> {
        val email = sampleService.createSingleDummyAccount(
            testEmail,
            testNickname,
            testBirthDate,
            testGender,
        )
        return email.succeed()
    }

    /**
     * 개발용 테스트 유저 로그인입니다.
     * 사용에 주의해주세요.
     */
    @DisableSwaggerAuthButton
    @Operation(
        summary = "개발용 테스트 유저 로그인",
        description = "개발용 테스트 유저입니다."
    )
    @GetMapping("/test/sign-in")
    fun testSignIn(@RequestParam email: String, @RequestParam expSec: Long): CaramelApiResponse<SignInResponse> {
        return sampleService.testSignIn(email, expSec).succeed()
    }

    @PreAuthorize("hasRole('SINGLE')")
    @GetMapping("/is-single")
    fun getSingle(): String {
        return "single"
    }

    @PreAuthorize("hasRole('COUPLED')")
    @GetMapping("/is-couple")
    fun getCouple(): String {
        return "couple"
    }
}