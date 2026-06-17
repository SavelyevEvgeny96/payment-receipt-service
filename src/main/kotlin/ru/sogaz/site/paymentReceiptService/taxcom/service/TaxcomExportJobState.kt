package ru.sogaz.site.paymentReceiptService.taxcom.service

import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

@Component
class TaxcomExportJobState {
    private val running = AtomicBoolean(false)
    @Volatile
    var lastStartedAt: Instant? = null
        private set
    @Volatile
    var lastFinishedAt: Instant? = null
        private set
    @Volatile
    var status: String = "IDLE"
        private set

    fun start(): Boolean =
        running.compareAndSet(false, true).also { started ->
            if (started) {
                lastStartedAt = Instant.now()
                status = "RUNNING"
            }
        }

    fun finish(success: Boolean, message: String? = null) {
        lastFinishedAt = Instant.now()
        status = if (success) "COMPLETED" else "FAILED${message?.let { ": $it" } ?: ""}"
        running.set(false)
    }

    fun snapshot(): String = status
}
