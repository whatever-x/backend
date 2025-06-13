package com.whatever.domain.user.controller

import com.whatever.domain.user.dto.GetUserInfoResponse
import com.whatever.domain.user.dto.PatchUserSettingRequest
import com.whatever.domain.user.dto.UserSettingResponse
import com.whatever.domain.user.dto.PostUserProfileRequest
import com.whatever.domain.user.dto.PostUserProfileResponse
import com.whatever.domain.user.dto.PutUserProfileRequest
import com.whatever.domain.user.dto.PutUserProfileResponse
import com.whatever.domain.user.service.UserService
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@Tag(
    name = "User",
    description = "유저 API"
)
@RestController
@RequestMapping("/v1/user")
class UserController(
    private val userService: UserService
) {
    @Operation(
        summary = "프로필 수정",
        description = "닉네임/생일을 수정합니다.",
    )
    @PutMapping("/profile")
    fun updateProfile(
        @Valid @RequestBody putUserProfileRequest: PutUserProfileRequest,
    ): CaramelApiResponse<PutUserProfileResponse> {
        val userProfileResponse = userService.updateProfile(putUserProfileRequest)
        return userProfileResponse.succeed()
    }

    @Operation(
        summary = "프로필 생성",
        description = "약관 동의 여부와 함께 프로필을 생성합니다.",
    )
    @PostMapping("/profile")
    fun createProfile(
        @Valid @RequestBody postUserProfileRequest: PostUserProfileRequest
    ): CaramelApiResponse<PostUserProfileResponse> {
        val userProfileResponse = userService.createProfile(postUserProfileRequest)
        return userProfileResponse.succeed()
    }

    @Operation(
        summary = "내 정보 조회",
        description = "해당하는 유저 정보를 조회합니다. 개인의 정보만 조회 가능합니다."
    )
    @GetMapping("/me")
    fun getUserInfo(): CaramelApiResponse<GetUserInfoResponse> {
        val response = userService.getUserInfo()
        return response.succeed()
    }

    @Operation(
        summary = "유저 설정 수정",
        description = "수정할 설정 정보만 추가하여 보내면 됩니다. 아무것도 넣지 않을경우 현재 설정 정보를 그대로 반환합니다.",
    )
    @PatchMapping("/settings")
    fun updateUserSetting(
        @RequestBody request: PatchUserSettingRequest
    ): CaramelApiResponse<UserSettingResponse> {
        val response = userService.updateUserSetting(request)
        return response.succeed()
    }

    @Operation(
        summary = "유저 설정 조회",
        description = "설정을 조회합니다.",
    )
    @GetMapping("/settings")
    fun getUserSetting(): CaramelApiResponse<UserSettingResponse> {
        val response = userService.getUserSetting()
        return response.succeed()
    }
}