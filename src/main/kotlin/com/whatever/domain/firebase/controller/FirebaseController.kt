package com.whatever.domain.firebase.controller

import com.whatever.domain.firebase.controller.dto.request.SetFcmTokenRequest
import com.whatever.domain.firebase.service.FirebaseService
import com.whatever.global.constants.CaramelHttpHeaders.DEVICE_ID
import com.whatever.global.exception.dto.CaramelApiResponse
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

    @PostMapping
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