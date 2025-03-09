package com.whatever.domain.couple.controller

import com.whatever.domain.couple.controller.dto.request.CreateCoupleRequest
import com.whatever.domain.couple.controller.dto.request.UpdateCoupleSharedMessageRequest
import com.whatever.domain.couple.controller.dto.request.UpdateCoupleStartDateRequest
import com.whatever.domain.couple.controller.dto.response.CoupleBasicResponse
import com.whatever.domain.couple.controller.dto.response.CoupleInvitationCodeResponse
import com.whatever.domain.couple.controller.dto.response.CoupleUserInfoDto
import com.whatever.domain.couple.controller.dto.response.CoupleDetailResponse
import com.whatever.domain.couple.service.CoupleService
import com.whatever.global.exception.dto.CaramelApiResponse
import com.whatever.global.exception.dto.succeed
import com.whatever.util.DateTimeUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
        summary = "더미 커플 정보 조회",
        description = "커플 정보를 조회합니다."
    )
    @GetMapping("/{couple-id}")
    fun getCoupleInfo(@PathVariable("couple-id") coupleId: Long): CaramelApiResponse<CoupleDetailResponse> {

        // TODO(준용): 구현 필요
        return CoupleDetailResponse(
            coupleId = coupleId,
            startDate = DateTimeUtil.localNow().toLocalDate(),
            sharedMessage = "공유메시지",
            myInfo = CoupleUserInfoDto(
                id = 1L,
                nickname = "내 닉네임",
                birthDate = DateTimeUtil.localNow().toLocalDate()
            ),
            partnerInfo = CoupleUserInfoDto(
                id = 2L,
                nickname = "상대방 닉네임",
                birthDate = DateTimeUtil.localNow().toLocalDate()
            )
        ).succeed()
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
        summary = "더미 커플 연결",
        description = "초대 코드를 사용해 커플을 연결(생성)합니다."
    )
    @PostMapping("/connect")
    fun createCouple(@RequestBody request: CreateCoupleRequest): CaramelApiResponse<CoupleDetailResponse> {

        // TODO(준용): 구현 필요
        return CoupleDetailResponse(
            coupleId = 1L,
            startDate = DateTimeUtil.localNow().toLocalDate(),
            sharedMessage = "공유메시지",
            myInfo = CoupleUserInfoDto(
                id = 1L,
                nickname = "내 닉네임",
                birthDate = DateTimeUtil.localNow().toLocalDate()
            ),
            partnerInfo = CoupleUserInfoDto(
                id = 2L,
                nickname = "상대방 닉네임",
                birthDate = DateTimeUtil.localNow().toLocalDate()
            )
        ).succeed()
    }

    @Operation(
        summary = "더미 커플 시작일 수정",
        description = "커플의 시작일을 수정합니다. 미래는 불가능합니다."
    )
    @PatchMapping("/{couple-id}/start-date")
    fun updateCoupleStartDate(
        @PathVariable("couple-id") coupleId: Long,
        @RequestBody request: UpdateCoupleStartDateRequest,
    ): CaramelApiResponse<CoupleBasicResponse> {

        // TODO(준용): 구현 필요
        return CoupleBasicResponse(
            coupleId = 1L,
            startDate = request.startDate,
            sharedMessage = null,
        ).succeed()
    }

    @Operation(
        summary = "더미 커플 공유 메시지 수정",
        description = "커플의 공유 메시지를 수정합니다."
    )
    @PatchMapping("/{couple-id}/shared-message")
    fun updateCoupleSharedMessage(
        @PathVariable("couple-id") coupleId: Long,
        @RequestBody request: UpdateCoupleSharedMessageRequest,
    ): CaramelApiResponse<CoupleBasicResponse> {

        // TODO(준용): 구현 필요
        return CoupleBasicResponse(
            coupleId = 1L,
            startDate = null,
            sharedMessage =
                if (request.sharedMessage?.isBlank() == true) null
                else request.sharedMessage,
        ).succeed()
    }

    @Operation(
        summary = "더미 커플 삭제",
        description = "커플을 삭제하고, 관련 정보를 모두 삭제합니다."
    )
    @DeleteMapping("/{couple-id}")
    fun deleteCouple(@PathVariable("couple-id") coupleId: Long): CaramelApiResponse<Unit> {

        // TODO(준용): 구현 필요
        return CaramelApiResponse.succeed()
    }
}