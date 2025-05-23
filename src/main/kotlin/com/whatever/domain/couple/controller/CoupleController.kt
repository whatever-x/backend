package com.whatever.domain.couple.controller

import com.whatever.domain.couple.controller.dto.request.CreateCoupleRequest
import com.whatever.domain.couple.controller.dto.request.UpdateCoupleSharedMessageRequest
import com.whatever.domain.couple.controller.dto.request.UpdateCoupleStartDateRequest
import com.whatever.domain.couple.controller.dto.response.CoupleBasicResponse
import com.whatever.domain.couple.controller.dto.response.CoupleInvitationCodeResponse
import com.whatever.domain.couple.controller.dto.response.CoupleDetailResponse
import com.whatever.domain.couple.service.CoupleService
import com.whatever.global.constants.CaramelHttpHeaders.TIME_ZONE
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Couple",
    description = "커플 API"
)
@RestController
@RequestMapping("/v1/couples")
class CoupleController(
    private val coupleService: CoupleService
) {

    @Operation(
        summary = "내 커플 정보 조회",
        description = "유저 정보를 제외한 내 커플 정보를 반환합니다."
    )
    @GetMapping("/me")
    fun getMyCoupleInfo(): CaramelApiResponse<CoupleBasicResponse> {
        val response = coupleService.getCoupleInfo()
        return response.succeed()
    }


    @Operation(
        summary = "커플 정보 조회",
        description = "커플 정보를 조회합니다."
    )
    @GetMapping("/{couple-id}")
    fun getCoupleInfo(@PathVariable("couple-id") coupleId: Long): CaramelApiResponse<CoupleDetailResponse> {
        val response = coupleService.getCoupleAndMemberInfo(coupleId)
        return response.succeed()
    }

    @Operation(
        summary = "커플 초대 코드 생성",
        description = "커플 초대 코드를 생성합니다. 커플이 아닌 유저만 접근 가능합니다."
    )
    @PostMapping("/invitation-code")
    fun createInvitationCode(): CaramelApiResponse<CoupleInvitationCodeResponse> {
        val response = coupleService.createInvitationCode()
        return response.succeed()
    }

    @Operation(
        summary = "커플 연결",
        description = "초대 코드를 사용해 커플을 연결(생성)합니다."
    )
    @PostMapping("/connect")
    fun createCouple(@RequestBody request: CreateCoupleRequest): CaramelApiResponse<CoupleDetailResponse> {
        val response = coupleService.createCouple(request)
        return response.succeed()
    }

    @Operation(
        summary = "커플 시작일 수정",
        description = "커플의 시작일을 수정합니다. 미래는 불가능합니다."
    )
    @PatchMapping("/{couple-id}/start-date")
    fun updateCoupleStartDate(
        @PathVariable("couple-id") coupleId: Long,
        @RequestBody request: UpdateCoupleStartDateRequest,
        @RequestHeader(TIME_ZONE) timeZone: String,
    ): CaramelApiResponse<CoupleBasicResponse> {
        val response = coupleService.updateStartDate(coupleId, request, timeZone)
        return response.succeed()
    }

    @Operation(
        summary = "커플 공유 메시지 수정",
        description = "커플의 공유 메시지를 수정합니다."
    )
    @PatchMapping("/{couple-id}/shared-message")
    fun updateCoupleSharedMessage(
        @PathVariable("couple-id") coupleId: Long,
        @RequestBody request: UpdateCoupleSharedMessageRequest,
    ): CaramelApiResponse<CoupleBasicResponse> {
        val response = coupleService.updateSharedMessage(coupleId, request)
        return response.succeed()
    }

    @Operation(
        summary = "커플 나가기",
        description = "커플을 나가고, 지금까지 작성한 모든 데이터를 삭제합니다."
    )
    @DeleteMapping("/{couple-id}/members/me")
    fun leaveCouple(@PathVariable("couple-id") coupleId: Long): CaramelApiResponse<Unit> {
        coupleService.leaveCouple(coupleId)
        return CaramelApiResponse.succeed()
    }
}