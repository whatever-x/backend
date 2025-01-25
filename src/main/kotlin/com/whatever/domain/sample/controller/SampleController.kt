package com.whatever.domain.sample.controller

import com.whatever.domain.sample.exception.SampleExceptionCode
import com.whatever.domain.sample.exception.SampleNotFoundException
import com.whatever.domain.sample.service.SampleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController(
    private val sampleService: SampleService,
) {

    @GetMapping("/sample")
    fun getSample(): ResponseEntity<String> {
        val result: String = sampleService.getSample()
        return ResponseEntity.ok(result)
    }

    @GetMapping("/exception")
    fun getException(): ResponseEntity<String> {
        throw SampleNotFoundException(SampleExceptionCode.SAMPLE_CODE, "예외 생성 예시입니다.")
        return ResponseEntity.ok("exception not found")
    }
}