package com.whatever.caramel.batch.config.listener

import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.annotation.AfterJob
import org.springframework.stereotype.Component

@Component
class AnniversaryJobListener {

    @AfterJob
    fun finishAnniversaryJob(jobExecution: JobExecution) {
        // TODO 슬랙으로 쏘는 것 추가 예정
        when (jobExecution.status) {
            BatchStatus.COMPLETED -> TODO()
            BatchStatus.STARTING -> TODO()
            BatchStatus.STARTED -> TODO()
            BatchStatus.STOPPING -> TODO()
            BatchStatus.STOPPED -> TODO()
            BatchStatus.FAILED -> TODO()
            BatchStatus.ABANDONED -> TODO()
            BatchStatus.UNKNOWN -> TODO()
        }
    }
}
