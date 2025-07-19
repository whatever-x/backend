package com.whatever.com.whatever.caramel.api.sample.controller

import com.whatever.CaramelApiResponse
import com.whatever.caramel.common.global.annotation.DisableSwaggerAuthButton
import com.whatever.com.whatever.caramel.api.auth.dto.SignInResponse
import com.whatever.com.whatever.caramel.api.sample.controller.dto.SampleSendFcmRequest
import com.whatever.domain.sample.service.SampleService
import com.whatever.domain.user.model.UserGender
import com.whatever.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Profile("dev", "local-mem")
@Tag(
    name = "샘플 API",
    description = "개발 및 테스트를 위한 간단한 예제 API 입니다. 운영환경에서는 노출되어서는 안됩니다."
)
@RestController
@RequestMapping("/sample")
class SampleController(
    private val sampleService: SampleService,
) {

    @DisableSwaggerAuthButton
    @Operation(
        summary = "FCM Notification 테스트",
        description = """
            ### 지정된 사용자들에게 테스트 FCM 푸시 알림을 전송합니다.
            
            **사전 조건**
            - `targetUserIds`에 해당하는 유저의 FCM 토큰이 DB에 저장되어 있어야 합니다.
            
            **참고**
            - `title`, `body`는 `com.google.firebase.messaging.Notification`과 동일합니다.
        """,
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
        summary = "단순 문자열을 반환하는 api 테스트",
        description = """
            ### 통신을 테스트하기 위해 단순 문자열을 반환합니다.
        """,
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
        summary = "개발용 NEW 유저 생성",
        description = """
            ### NEW 상태인 유저를 생성하고 해당 이메일을 반환합니다.
            
            **참고**
            - 이메일을 지정하지 않으면 랜덤한 값으로 생성합니다.
        """,
    )
    @GetMapping("/test/sign-up/new")
    fun testSignUpNew(
        @Parameter(description = "지정할 테스트 이메일 (미지정 시 랜덤 생성)")
        @RequestParam(required = false) testEmail: String? = null,
    ): CaramelApiResponse<String> {
        val email = sampleService.createNewDummyAccount(testEmail)
        return email.succeed()
    }

    @DisableSwaggerAuthButton
    @Operation(
        summary = "개발용 SINGLE 유저 생성",
        description = """
            ### SINGLE 상태인 유저를 생성하고 해당 이메일을 반환합니다.
            
            **참고**
            - 이메일: 지정하지 않으면 랜덤한 값으로 설정
            - 닉네임: 지정하지 않으면 랜덤한 값으로 설정
            - 생일: 지정하지 않으면 서버시간을 기준으로 현재시간으로 설정
            - 성별: 지정하지 않으면 `MALE`로 설정
        """,
    )
    @GetMapping("/test/sign-up/single")
    fun testSignUpSingle(
        @Parameter(description = "지정할 테스트 이메일 (미지정 시 랜덤 생성)")
        @RequestParam(required = false) testEmail: String? = null,
        @Parameter(description = "지정할 테스트 닉네임 (미지정 시 랜덤 생성)")
        @RequestParam(required = false) testNickname: String? = null,
        @Parameter(description = "지정할 테스트 생일 (미지정 시 서버시간을 기준으로 현재로 설정)")
        @RequestParam(required = false) testBirthDate: LocalDate? = null,
        @Parameter(description = "지정할 테스트 성별 (미지정 시 MALE)")
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

    @DisableSwaggerAuthButton
    @Operation(
        summary = "테스트 유저 로그인",
        description = """
            ### 이메일을 사용하여 테스트 유저로 로그인하고 jwt를 발급받습니다.
            
            **사전 조건**
            - 로그인할 이메일로 가입된 유저가 존재해야합니다
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "JWT(access, refresh) 정보 및 유저 정보 반환")
        ]
    )
    @GetMapping("/test/sign-in")
    fun testSignIn(
        @Parameter(description = "로그인할 테스트 유저의 이메일")
        @RequestParam email: String,
        @Parameter(description = "access 토큰 만료 시간(초)", example = "3600")
        @RequestParam expSec: Long,
    ): CaramelApiResponse<SignInResponse> {
        return sampleService.testSignIn(email, expSec).let {
            SignInResponse.from(it)
        }.succeed()
    }

    @Operation(
        summary = "SINGLE 권환 확인 테스트",
        description = """
            ### 해당 jwt를 사용하는 유저가 SINGLE 상태인지 확인합니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "성공 시 'single' 문자열 반환"),
        ],
    )
    @PreAuthorize("hasRole('SINGLE')")
    @GetMapping("/is-single")
    fun getSingle(): String {
        return "single"
    }

    @Operation(
        summary = "COUPLED 권환 확인 테스트",
        description = """### 해당 jwt를 사용하는 유저가 COUPLED 상태인지 확인합니다.""",
        responses = [
            ApiResponse(responseCode = "200", description = "성공 시 'couple' 문자열 반환"),
        ],
    )
    @PreAuthorize("hasRole('COUPLED')")
    @GetMapping("/is-couple")
    fun getCouple(): String {
        return "couple"
    }
}
