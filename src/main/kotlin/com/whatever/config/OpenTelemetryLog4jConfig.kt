package com.whatever.config

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.log4j.appender.v2_17.OpenTelemetryAppender
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class OpenTelemetryConfig(
    private val openTelemetry: OpenTelemetry,
) {
    @PostConstruct
    private fun installOpenTelemetry() {
        OpenTelemetryAppender.install(this.openTelemetry);
    }
}