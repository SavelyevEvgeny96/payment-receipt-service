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
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val traceId = request.getHeader("TraceId") ?: UUID.randomUUID().toString()

            MDC.put("traceId", traceId)

            filterChain.doFilter(request, response)
        } finally {
            MDC.remove("traceId")
        }
    }
}
