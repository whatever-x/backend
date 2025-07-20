package com.whatever.caramel.api.couple.controller

import com.whatever.CaramelApiResponse
import com.whatever.SecurityUtil.getCurrentUserCoupleId
import com.whatever.SecurityUtil.getCurrentUserId
import com.whatever.caramel.api.couple.controller.dto.request.CreateCoupleRequest
import com.whatever.caramel.api.couple.controller.dto.request.UpdateCoupleSharedMessageRequest
import com.whatever.caramel.api.couple.controller.dto.request.UpdateCoupleStartDateRequest
import com.whatever.caramel.api.couple.controller.dto.response.CoupleAnniversaryResponse
import com.whatever.caramel.api.couple.controller.dto.response.CoupleBasicResponse
import com.whatever.caramel.api.couple.controller.dto.response.CoupleDetailResponse
import com.whatever.caramel.api.couple.controller.dto.response.CoupleInvitationCodeResponse
import com.whatever.caramel.common.global.constants.CaramelHttpHeaders.TIME_ZONE
import com.whatever.domain.couple.model.Couple.Companion.MAX_SHARED_MESSAGE_LENGTH
import com.whatever.domain.couple.service.CoupleAnniversaryService
import com.whatever.domain.couple.service.CoupleService
import com.whatever.succeed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Tag(
    name = "커플 API",
    description = "커플 관련 기능을 제공하는 API"
)
@RestController
@RequestMapping("/v1/couples")
class CoupleController(
    private val coupleService: CoupleService,
    private val coupleAnnivService: CoupleAnniversaryService,
) {

    @Operation(
        summary = "내 커플 정보 조회",
        description = """
            ### 내 커플 정보를 조회합니다.
            
            - 커플의 정보만을 조회합니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "커플 정보"),
        ]
    )
    @GetMapping("/me")
    fun getMyCoupleInfo(): CaramelApiResponse<CoupleBasicResponse> {
        val coupleVo = coupleService.getCoupleInfo(getCurrentUserCoupleId())
        return CoupleBasicResponse.from(coupleVo).succeed()
    }

    @Operation(
        summary = "커플 상세 정보 조회",
        description = """
            ### couple-id에 해당하는 커플 상세 정보를 반환합니다.
            
            - 나의 커플만 조회할 수 있습니다.
            - 커플과 커플 멤버들의 정보를 조회합니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "커플 + 커플 멤버 정보"),
        ]
    )
    @GetMapping("/{couple-id}")
    fun getCoupleInfo(@PathVariable("couple-id") coupleId: Long): CaramelApiResponse<CoupleDetailResponse> {
        val coupleDetailVo = coupleService.getCoupleAndMemberInfo(
            coupleId = getCurrentUserCoupleId(),
            currentUserId = getCurrentUserId()
        )
        return CoupleDetailResponse.from(coupleDetailVo).succeed()
    }

    @Operation(
        summary = "커플 기념일 조회",
        description = """
            ### 조회 범위에 속하는 `커플 기념일`과 `커플 정보(시작일, 공유 메시지)`를 조회합니다.
            
            - 기념일 포함 항목
                - 100일 단위 기념일 (최대 300일까지 포함)
                - 1주년 단위 기념일
                - 내 생일
                - 상대방 생일
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "커플 기념일 + 커플 정보(시작일, 공유메시지)"),
        ]
    )
    @GetMapping("/{couple-id}/anniversaries")
    fun getCoupleAnniversaries(
        @PathVariable("couple-id") coupleId: Long,

        @Parameter(description = "조회 시작일")
        @RequestParam("startDate") startDate: LocalDate,

        @Parameter(description = "조회 종료일")
        @RequestParam("endDate") endDate: LocalDate,
    ): CaramelApiResponse<CoupleAnniversaryResponse> {
        val coupleAnniversaryVo = coupleAnnivService.getCoupleAnniversary(
            startDate = startDate,
            endDate = endDate,
            coupleId = getCurrentUserCoupleId(),
            requestUserId = getCurrentUserId(),
        )
        return CoupleAnniversaryResponse.from(coupleAnniversaryVo).succeed()
    }

    @Operation(
        summary = "커플 초대 코드 생성",
        description = """
            ### 커플 초대 코드를 생성합니다.
            
            - `SINGLE` 유저만 이용할 수 있습니다.
            - 코드가 만료되기 전까지는 반복적으로 호출해도 항상 동일한 코드가 반환됩니다.
            - 반복적으로 호출해도 만료기간은 동일합니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "초대 코드 정보"),
        ]
    )
    @PostMapping("/invitation-code")
    fun createInvitationCode(): CaramelApiResponse<CoupleInvitationCodeResponse> {
        val coupleInvitationCodeVo = coupleService.createInvitationCode(userId = getCurrentUserId())
        return CoupleInvitationCodeResponse.from(coupleInvitationCodeVo).succeed()
    }

    @Operation(
        summary = "커플 연결",
        description = """
            ### 커플을 연결합니다.
            
            - 두 유저 모두 `SINGLE` 상태여야 합니다.
            - 연결이 완료된 후 초대 코드는 재사용할 수 없습니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "커플 상세 정보"),
        ]
    )
    @PostMapping("/connect")
    fun createCouple(@RequestBody request: CreateCoupleRequest): CaramelApiResponse<CoupleDetailResponse> {
        val coupleDetailVo = coupleService.createCouple(
            invitationCode = request.invitationCode,
            joinerUserId = getCurrentUserId(),
        )
        return CoupleDetailResponse.from(coupleDetailVo).succeed()
    }

    @Operation(
        summary = "커플 시작일 수정",
        description = """
            ### 커플의 시작일을 수정합니다.
            
            - 함께 전송한 Time-Zone을 기준으로 미래 시간은 불가능합니다.
            - 커플 중 한명이 탈퇴하여 INACTIVE 상태라면 사용할 수 없습니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "수정된 커플 정보"),
        ]
    )
    @PatchMapping("/{couple-id}/start-date")
    fun updateCoupleStartDate(
        @PathVariable("couple-id") coupleId: Long,
        @RequestBody request: UpdateCoupleStartDateRequest,
        @RequestHeader(TIME_ZONE) timeZone: String,
    ): CaramelApiResponse<CoupleBasicResponse> {
        val coupleVo = coupleService.updateStartDate(
            coupleId = getCurrentUserCoupleId(),
            newCoupleStartDate = request.startDate,
            timeZone = timeZone,
        )
        return CoupleBasicResponse.from(coupleVo).succeed()
    }

    @Operation(
        summary = "커플 공유 메시지 수정",
        description = """
            ### 커플의 공유 메시지를 수정합니다.
            
            - 문자 개수를 기준으로 ${MAX_SHARED_MESSAGE_LENGTH}까지 허용합니다.
            - Null 혹은 Blank 문자열은 공유 메시지가 없는 상태로 취급합니다.
            - 커플 중 한명이 탈퇴하여 INACTIVE 상태라면 사용할 수 없습니다.
        """,
        responses = [
            ApiResponse(responseCode = "200", description = "수정된 커플 정보"),
        ]
    )
    @PatchMapping("/{couple-id}/shared-message")
    fun updateCoupleSharedMessage(
        @PathVariable("couple-id") coupleId: Long,
        @Valid @RequestBody request: UpdateCoupleSharedMessageRequest,
    ): CaramelApiResponse<CoupleBasicResponse> {
        val coupleVo = coupleService.updateSharedMessage(
            coupleId = getCurrentUserCoupleId(),
            newCoupleSharedMessage = request.sharedMessage,
        )
        return CoupleBasicResponse.from(coupleVo).succeed()
    }

    @Operation(
        summary = "커플 나가기",
        description = """
            ### 소속한 커플을 탈퇴합니다.
            
            - 작성했던 모든 데이터를 삭제합니다.
            - 탈퇴한 유저는 SINGLE 상태로 변경됩니다.
        """,
    )
    @DeleteMapping("/{couple-id}/members/me")
    fun leaveCouple(@PathVariable("couple-id") coupleId: Long): CaramelApiResponse<Unit> {
        coupleService.leaveCouple(
            coupleId = getCurrentUserCoupleId(),
            userId = getCurrentUserId(),
        )
        return CaramelApiResponse.succeed()
    }
}
