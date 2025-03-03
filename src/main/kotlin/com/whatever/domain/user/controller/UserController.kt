package com.whatever.domain.user.controller

import com.whatever.domain.user.dto.PostUserProfileRequest
import com.whatever.domain.user.dto.PostUserProfileResponse
import com.whatever.domain.user.dto.PutUserProfileRequest
import com.whatever.domain.user.dto.PutUserProfileResponse
import com.whatever.domain.user.service.UserService
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(
    name = "User",
    description = "유저 API"
)
@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService
) {
    @Operation(
        summary = "프로필 수정",
        description = "닉네임/생일을 수정합니다.",
    )
    @PutMapping("/profile")
    fun updateProfile(
        @RequestBody putUserProfileRequest: PutUserProfileRequest,
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
        @RequestBody postUserProfileRequest: PostUserProfileRequest
    ): CaramelApiResponse<PostUserProfileResponse> {
        val userProfileResponse = userService.createProfile(postUserProfileRequest)
        return userProfileResponse.succeed()
    }
}