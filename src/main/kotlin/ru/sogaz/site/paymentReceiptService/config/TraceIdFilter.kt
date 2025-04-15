package ru.sogaz.site.paymentReceiptService.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class TraceIdFilter : OncePerRequestFilter() {
    companion object {
        private const val TRACE_ID_HEADER = "TraceId"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val traceId = request.getHeader(TRACE_ID_HEADER) ?: UUID.randomUUID().toString()
        MDC.put("traceId", traceId)

        try {
            logRequest(request)
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove("traceId")
        }
    }

    private fun logRequest(request: HttpServletRequest) {
        val method = request.method
        val uri = request.requestURI
        val queryString = request.queryString?.let { "?$it" } ?: ""
        val parameters =
            request.parameterMap.entries.joinToString(", ") { (key, values) ->
                "$key=${values.joinToString(",")}"
            }

        logger.info("HTTP $method: $uri$queryString. Parameters: $parameters")
    }
}
