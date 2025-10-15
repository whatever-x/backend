package com.whatever.caramel.batch.config.listener

import com.slack.api.Slack
import com.slack.api.webhook.Payload
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterStep
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AnniversaryStepListener {
    @Value("\${slack.webhook.url}")
    private lateinit var webhookUrl: String

    @AfterStep
    fun afterStep(stepExecution: StepExecution): ExitStatus {
        val slack = Slack.getInstance()
        val message = "${stepExecution.writeCount}명 대상으로 FCM 발송"
        val payLoad = Payload.builder()
            .text(message)
            .build()
        slack.send(
            webhookUrl,
            payLoad
        )
        return ExitStatus.COMPLETED
    }
}
