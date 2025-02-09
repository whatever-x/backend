package com.whatever.domain.sample.service

import com.whatever.domain.sample.repository.SampleEntityRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SampleService(
    sampleEntityRepository: SampleEntityRepository
) {

    fun getSample(): String {
        return "sample String"
    }

}