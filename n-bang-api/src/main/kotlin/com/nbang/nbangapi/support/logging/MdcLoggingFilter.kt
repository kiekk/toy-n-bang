package com.nbang.nbangapi.support.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class MdcLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val TRACE_ID = "traceId"
        const val REQUEST_ID = "requestId"
        const val CLIENT_IP = "clientIp"
        const val REQUEST_URI = "requestUri"
        const val REQUEST_METHOD = "requestMethod"

        private const val HEADER_X_REQUEST_ID = "X-Request-ID"
        private const val HEADER_X_TRACE_ID = "X-Trace-ID"
        private const val HEADER_X_FORWARDED_FOR = "X-Forwarded-For"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val startTime = System.currentTimeMillis()

        try {
            setupMdc(request)
            addResponseHeaders(response)

            logRequest(request)

            filterChain.doFilter(request, response)

            logResponse(request, response, startTime)
        } finally {
            clearMdc()
        }
    }

    private fun setupMdc(request: HttpServletRequest) {
        val traceId = request.getHeader(HEADER_X_TRACE_ID) ?: generateId()
        val requestId = request.getHeader(HEADER_X_REQUEST_ID) ?: generateId()
        val clientIp = extractClientIp(request)

        MDC.put(TRACE_ID, traceId)
        MDC.put(REQUEST_ID, requestId)
        MDC.put(CLIENT_IP, clientIp)
        MDC.put(REQUEST_URI, request.requestURI)
        MDC.put(REQUEST_METHOD, request.method)
    }

    private fun addResponseHeaders(response: HttpServletResponse) {
        MDC.get(TRACE_ID)?.let { response.setHeader(HEADER_X_TRACE_ID, it) }
        MDC.get(REQUEST_ID)?.let { response.setHeader(HEADER_X_REQUEST_ID, it) }
    }

    private fun logRequest(request: HttpServletRequest) {
        if (shouldSkipLogging(request)) return

        log.info(
            ">>> REQUEST: {} {} | Client: {}",
            request.method,
            request.requestURI,
            MDC.get(CLIENT_IP)
        )
    }

    private fun logResponse(
        request: HttpServletRequest,
        response: HttpServletResponse,
        startTime: Long
    ) {
        if (shouldSkipLogging(request)) return

        val duration = System.currentTimeMillis() - startTime

        log.info(
            "<<< RESPONSE: {} {} | Status: {} | Duration: {}ms",
            request.method,
            request.requestURI,
            response.status,
            duration
        )
    }

    private fun shouldSkipLogging(request: HttpServletRequest): Boolean {
        val uri = request.requestURI
        return uri.startsWith("/actuator") ||
                uri.startsWith("/favicon") ||
                uri.endsWith(".css") ||
                uri.endsWith(".js") ||
                uri.endsWith(".ico")
    }

    private fun extractClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader(HEADER_X_FORWARDED_FOR)
        return if (!xForwardedFor.isNullOrBlank()) {
            xForwardedFor.split(",").first().trim()
        } else {
            request.remoteAddr
        }
    }

    private fun clearMdc() {
        MDC.remove(TRACE_ID)
        MDC.remove(REQUEST_ID)
        MDC.remove(CLIENT_IP)
        MDC.remove(REQUEST_URI)
        MDC.remove(REQUEST_METHOD)
    }

    private fun generateId(): String = UUID.randomUUID().toString().replace("-", "").take(16)
}
