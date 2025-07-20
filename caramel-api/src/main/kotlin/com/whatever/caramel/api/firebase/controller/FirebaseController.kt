package com.whatever.caramel.api.firebase.controller

import com.whatever.CaramelApiResponse
import com.whatever.SecurityUtil
import com.whatever.caramel.api.firebase.controller.dto.request.SetFcmTokenRequest
import com.whatever.caramel.common.global.constants.CaramelHttpHeaders.DEVICE_ID
import com.whatever.firebase.service.FirebaseService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Firebase API",
    description = "Fcm token 등 Firebase 관련 기능을 제공하는 API",
)
@RestController
@RequestMapping("/v1/firebase")
class FirebaseController(
    private val firebaseService: FirebaseService,
) {

    @Operation(
        summary = "fcm token 업데이트",
        description = """
            ### FCM Token을 설정합니다.
            
            - fcm토큰의 수명 관리를 위해 주기적으로 호출해야 하는 api입니다.
            - 같은 토큰이더라도 270일동안 해당 api로 호출하지 않았다면 비활성화 됩니다.
        """,
    )
    @PostMapping("/fcm")
    fun setFcmToken(
        @Parameter(description = "현재 요청을 보낸 Device의 고유한 id")
        @RequestHeader(name = DEVICE_ID, required = true) deviceId: String,
        @RequestBody request: SetFcmTokenRequest,
    ): CaramelApiResponse<Unit> {
        firebaseService.setFcmToken(
            deviceId = deviceId,
            token = request.token,
            userId = SecurityUtil.getCurrentUserId(),
        )
        return CaramelApiResponse.succeed()
    }
}
