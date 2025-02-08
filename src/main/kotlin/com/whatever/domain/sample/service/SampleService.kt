package com.whatever.domain.sample.service

import com.whatever.domain.sample.controller.SampleRequestDto
import com.whatever.domain.sample.exception.*
import com.whatever.domain.sample.repository.SampleEntityRepository
import com.whatever.util.DateTimeUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class SampleService (
    sampleEntityRepository: SampleEntityRepository
) {

    fun getSample(): SampleGetResultDto {
        return SampleGetResultDto(
            name = "Caramel",
            localDateTime = DateTimeUtil.localNow(),
            detail = SampleInnerDto(
                "중첩 Json 데이터입니다!"
            ),
            detailArray = mutableListOf(
                SampleInnerDto("요소1"),
                SampleInnerDto("요소2"),
                SampleInnerDto("요소3"),
            )
        )
    }

    fun postSample(request: SampleRequestDto): SamplePostResultDto {
        return SamplePostResultDto(
            result = "안녕하세요 ${request.name}님!",
            detail = "고생 많으십니다!",
        )
    }

    fun getException(exceptionNumber: Int) {
        when (exceptionNumber) {
            400 -> throw SampleClientException(SampleExceptionCode.SAMPLE_CODE, "에러 테스트 성공입니다!")
            401 -> throw SampleUnauthorizedException(SampleExceptionCode.SAMPLE_UNAUTHORIZED, "에러 테스트 성공입니다!")
            403 -> throw SampleForbiddenException(SampleExceptionCode.SAMPLE_FORBIDDEN, "에러 테스트 성공입니다!")
            500 -> throw SampleServerException(SampleExceptionCode.SAMPLE_SERVER_ERROR, "에러 테스트 성공입니다!")
            else -> throw SampleNotFoundException(SampleExceptionCode.SAMPLE_NOT_FOUND, "지원하는 에러코드는 400, 404, 500 입니다. 입력값:${exceptionNumber}")
        }
    }

}

data class SampleGetResultDto(
    val name: String,
    val localDateTime: LocalDateTime,
    val detail: SampleInnerDto,
    val detailArray: List<SampleInnerDto>,
)

data class SampleInnerDto(
    val description: String,
)

data class SamplePostResultDto(
    val result: String,
    val detail: String,
)