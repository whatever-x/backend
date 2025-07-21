package com.whatever.caramel.security.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.whatever.caramel.common.global.constants.CaramelHttpHeaders
import com.whatever.caramel.security.util.SecurityUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.StandardCharsets

private val requestFilterLogger = KotlinLogging.logger { }

@Component
class RequestResponseLoggingFilter(
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return !LOGGING_PATTERN.containsMatchIn(request.requestURI)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val wrappedRequest = ContentCachingRequestWrapper(request).apply {
            characterEncoding = StandardCharsets.UTF_8.name()
        }
        val wrappedResponse = ContentCachingResponseWrapper(response).apply {
            characterEncoding = StandardCharsets.UTF_8.name()
        }

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse)
        } finally {
            loggingRequest(wrappedRequest)
            loggingResponse(wrappedResponse)
            if (!response.isCommitted) {
                wrappedResponse.copyBodyToResponse()
            }
        }
    }

    private fun loggingRequest(
        request: ContentCachingRequestWrapper,
    ) {
        val requestDetails = mutableMapOf<String, Any>()

        requestDetails["user"] = runCatching { SecurityUtil.getCurrentUserId() }.getOrElse { "" }
        requestDetails["method"] = request.method
        requestDetails["uri"] = request.requestURI
        requestDetails["headers"] = getMaskedHeader(request.getHeaderMap())
        if (request.parameterMap.isNotEmpty()) {
            requestDetails["params"] = request.parameterMap
        }
        getBody(request.contentAsByteArray, request.characterEncoding)?.let {
            requestDetails["body"] = objectMapper.readBodySafely(it)
        }

        requestFilterLogger.info { objectMapper.writeValueAsString(requestDetails) }
    }

    private fun loggingResponse(
        response: ContentCachingResponseWrapper,
    ) {
        val responseDetails = mutableMapOf<String, Any>()
        responseDetails["status"] = response.status

        if (response.status >= 400) {
            getBody(response.contentAsByteArray, response.characterEncoding)?.let {
                responseDetails["exceptionBody"] = objectMapper.readBodySafely(it)
            }
        }

        val responseLog = objectMapper.writeValueAsString(responseDetails)
        when {
            response.status >= 500 -> requestFilterLogger.error { responseLog }
            response.status >= 400 -> requestFilterLogger.warn { responseLog }
            else -> requestFilterLogger.info { responseLog }
        }
    }

    private fun getBody(contentAsByteArray: ByteArray, characterEncoding: String?): String? {
        if (contentAsByteArray.isEmpty()) {
            return null
        }

        val body =
            try {
                String(contentAsByteArray, charset(characterEncoding ?: StandardCharsets.UTF_8.name()))
            } catch (e: Exception) {
                requestFilterLogger.warn(e) { "Failed to read body with encoding [${characterEncoding}]." }
                String(contentAsByteArray, StandardCharsets.UTF_8)
            }

        return body.takeIf { it.length <= MAX_BODY_LENGTH }
            ?: "${body.take(MAX_BODY_LENGTH)}...BODY_LENGTH:${body.length}"
    }

    private fun getMaskedHeader(headers: Map<String, String?>): Map<String, String> {
        return headers
            .filterKeys { key ->
                CARAMEL_HEADER_NAMES.contains(key.lowercase())
            }
            .mapValues { (key, value) ->
                if (SENSITIVE_HEADER_PATTERN.containsMatchIn(key)) {
                    MASK_VALUE
                } else {
                    value ?: ""
                }
            }
    }

    companion object {
        private const val MASK_VALUE = "****"
        private const val MAX_BODY_LENGTH = 500
        private val CARAMEL_HEADER_NAMES = CaramelHttpHeaders.ALL_HEADERS.map { it.lowercase() }.toSet()
        private val SENSITIVE_HEADER_PATTERN = Regex("(?i)authorization|device-id")
        private val LOGGING_PATTERN = Regex("(?i)/v1/")
    }
}

private fun ContentCachingRequestWrapper.getHeaderMap(): Map<String, String> {
    return headerNames?.asSequence()?.associateWith { getHeader(it) } ?: emptyMap()
}

private fun ObjectMapper.readBodySafely(body: String): Any {
    return try {
        readValue(body, Map::class.java)
    } catch (e: Exception) {
        body
    }
}
