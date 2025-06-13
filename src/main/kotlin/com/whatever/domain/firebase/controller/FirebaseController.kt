package com.whatever.domain.firebase.controller

import com.whatever.domain.firebase.controller.dto.request.SetFcmTokenRequest
import com.whatever.domain.firebase.service.FirebaseService
import com.whatever.global.constants.CaramelHttpHeaders.DEVICE_ID
import com.whatever.global.exception.dto.CaramelApiResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/firebase")
class FirebaseController(
    private val firebaseService: FirebaseService,
) {

    @Operation(
        summary = "fcm token 업데이트",
        description =
            "fcm토큰의 수명 관리를 위해 주기적으로 호출해야 하는 api입니다. <br>" +
            "같은 토큰이더라도 270일동안 해당 api로 호출하지 않았다면 비활성화 됩니다."
    )
    @PostMapping("/fcm")
    fun setFcmToken(
        @RequestHeader(name = DEVICE_ID, required = true) deviceId: String,
        @RequestBody request: SetFcmTokenRequest,
    ): CaramelApiResponse<Unit> {
        firebaseService.setFcmToken(
            deviceId = deviceId,
            token = request.token,
        )
        return CaramelApiResponse.succeed()
    }
}