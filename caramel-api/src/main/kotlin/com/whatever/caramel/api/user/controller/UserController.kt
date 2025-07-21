package com.whatever.caramel.api.user.controller

import com.whatever.caramel.common.response.CaramelApiResponse
import com.whatever.caramel.security.util.SecurityUtil.getCurrentUserId
import com.whatever.caramel.api.user.dto.GetUserInfoResponse
import com.whatever.caramel.api.user.dto.PatchUserSettingRequest
import com.whatever.caramel.api.user.dto.PostUserProfileRequest
import com.whatever.caramel.api.user.dto.PostUserProfileResponse
import com.whatever.caramel.api.user.dto.PutUserProfileRequest
import com.whatever.caramel.api.user.dto.PutUserProfileResponse
import com.whatever.caramel.api.user.dto.UserSettingResponse
import com.whatever.caramel.common.global.constants.CaramelHttpHeaders.TIME_ZONE
import com.whatever.caramel.common.util.toZoneId
import com.whatever.domain.user.service.UserService
import com.whatever.caramel.common.response.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "유저 API",
    description = "유저 프로필, 설정 등 유저 관련 기능을 제공하는 API"
)
@RestController
@RequestMapping("/v1/user")
class UserController(
    private val userService: UserService,
) {
    @Operation(
        summary = "프로필 수정",
        description = """
            ### 사용자의 닉네임과 생년월일을 수정합니다.
            
            - 수정하려는 값만 요청에 포함하면 됩니다.
            - 수정하지 않을 값은 포함하지 않거나, `null`로 전송해야 합니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "수정된 프로필 정보"),
        ],
    )
    @PutMapping("/profile")
    fun updateProfile(
        @Valid @RequestBody putUserProfileRequest: PutUserProfileRequest,
        @RequestHeader(TIME_ZONE) timeZone: String,
    ): CaramelApiResponse<PutUserProfileResponse> {
        val updatedUserProfileVo = userService.updateProfile(
            updateUserProfileVo = putUserProfileRequest.toVo(),
            userTimeZone = timeZone.toZoneId(),
            userId = getCurrentUserId(),
        )
        return PutUserProfileResponse.from(updatedUserProfileVo).succeed()
    }

    @Operation(
        summary = "프로필 생성",
        description = """
            ### 최초 로그인 후, 약관 동의 여부와 함께 프로필(닉네임, 생년월일)을 생성합니다.
            
            - 약관은 모두 동의되어야 합니다.
            - 프로필을 생성한 유저는 SINGLE 상태로 전환됩니다.
            - 프로필 생성 후, 알림 설정 API를 사용할 수 있습니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "생성된 프로필 정보"),
        ],
    )
    @PostMapping("/profile")
    fun createProfile(
        @Valid @RequestBody postUserProfileRequest: PostUserProfileRequest,
        @RequestHeader(TIME_ZONE) timeZone: String,
    ): CaramelApiResponse<PostUserProfileResponse> {
        val createdUserProfileVo = userService.createProfile(
            createUserProfileVo = postUserProfileRequest.toVo(),
            userTimeZone = timeZone.toZoneId(),
            userId = getCurrentUserId(),
        )
        return PostUserProfileResponse.from(createdUserProfileVo).succeed()
    }

    @Operation(
        summary = "내 정보 조회",
        description = """### 현재 로그인한 사용자의 정보를 조회합니다.""",
        responses = [
            ApiResponse(responseCode = "200", description = "유저 정보"),
        ],
    )
    @GetMapping("/me")
    fun getUserInfo(): CaramelApiResponse<GetUserInfoResponse> {
        val userInfoVo = userService.getUserInfo(getCurrentUserId())
        return GetUserInfoResponse.from(userInfoVo).succeed()
    }

    @Operation(
        summary = "유저 설정 수정",
        description = """
            ### 사용자의 알림설정 등을 수정합니다.
            
            - 수정하려는 설정 정보만 요청에 포함하면 됩니다.
            - 수정하지 않을 값은 포함하지 않거나, `null`로 전송해야 합니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "수정이 반영된 모든 설정 정보."),
        ],
    )
    @PatchMapping("/settings")
    fun updateUserSetting(
        @RequestBody request: PatchUserSettingRequest,
    ): CaramelApiResponse<UserSettingResponse> {
        val userSettingVo = userService.updateUserSetting(
            request = request.toVo(),
            userId = getCurrentUserId(),
        )
        return UserSettingResponse.from(userSettingVo).succeed()
    }

    @Operation(
        summary = "유저 설정 조회",
        description = """### 로그인한 유저의 설정을 조회합니다.""",
        responses = [
            ApiResponse(responseCode = "200", description = "유저의 설정 정보"),
        ],
    )
    @GetMapping("/settings")
    fun getUserSetting(): CaramelApiResponse<UserSettingResponse> {
        val userSettingVo = userService.getUserSetting(
            userId = getCurrentUserId(),
        )
        return UserSettingResponse.from(userSettingVo).succeed()
    }
}
