package com.whatever.domain.sample.controller

import com.whatever.domain.sample.service.SampleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController (
    private val sampleService: SampleService,
) {

    @GetMapping("/sample")
    fun getSample(): ResponseEntity<String> {
        val result: String = sampleService.getSample()
        return ResponseEntity.ok(result)
    }
}