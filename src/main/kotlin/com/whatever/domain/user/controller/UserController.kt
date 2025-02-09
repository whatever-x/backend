package com.whatever.domain.user.controller

import com.whatever.domain.user.dto.UserSignUpResponse
import com.whatever.domain.user.service.UserService
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
        description = "회원가입",
    )
    @PutMapping("/profile")
    fun postProfile(): CaramelApiResponse<UserSignUpResponse> {
        val signUpResponse = userService.createProfile()
        return signUpResponse.succeed()
    }
}