package com.whatever.caramel.batch.config.listener

import com.slack.api.Slack
import com.slack.api.webhook.Payload
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.annotation.AfterJob
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AnniversaryJobListener {
    @Value("\${slack.webhook.url}")
    private lateinit var webhookUrl: String

    @AfterJob
    fun finishAnniversaryJob(jobExecution: JobExecution) {
        if (webhookUrl.isBlank()) return
        val slack = Slack.getInstance()
        val batchStatusText = "FCM SEND ${jobExecution.status.name}"
        val message = when (jobExecution.status) {
            BatchStatus.STARTED,
            BatchStatus.STOPPING,
            BatchStatus.STOPPED,
            BatchStatus.STARTING -> return

            BatchStatus.COMPLETED -> batchStatusText
            BatchStatus.ABANDONED,
            BatchStatus.UNKNOWN,
            BatchStatus.FAILED -> "$batchStatusText ${jobExecution.failureExceptions}"
        }
        val payLoad = Payload.builder()
            .text(message)
            .build()
        slack.send(
            webhookUrl,
            payLoad
        )
    }
}
