package ru.sogaz.site.paymentReceiptService.taxcom.service

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomExportStatusResponse
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

@Component
class TaxcomExportRunState {
    private val running = AtomicBoolean(false)
    private val processedOutlets = AtomicLong(0)
    private val processedKkt = AtomicLong(0)
    private val processedShifts = AtomicLong(0)
    private val processedReceipts = AtomicLong(0)

    @Volatile
    private var status: String = STATUS_IDLE

    @Volatile
    private var currentStage: String? = null

    @Volatile
    private var lastError: String? = null

    @Volatile
    private var lastStartedAt: Instant? = null

    @Volatile
    private var lastFinishedAt: Instant? = null

    fun start(): Boolean =
        running.compareAndSet(false, true).also { started ->
            if (started) {
                processedOutlets.set(0)
                processedKkt.set(0)
                processedShifts.set(0)
                processedReceipts.set(0)
                lastStartedAt = Instant.now()
                lastFinishedAt = null
                lastError = null
                currentStage = STAGE_STARTING
                status = STATUS_RUNNING
            }
        }

    fun stage(stage: String) {
        currentStage = stage
    }

    fun outletProcessed() = processedOutlets.incrementAndGet()

    fun kktProcessed() = processedKkt.incrementAndGet()

    fun shiftProcessed() = processedShifts.incrementAndGet()

    fun receiptProcessed() = processedReceipts.incrementAndGet()

    fun finish(success: Boolean, message: String? = null) {
        lastFinishedAt = Instant.now()
        currentStage = null
        lastError = message
        status = if (success) STATUS_COMPLETED else STATUS_FAILED
        running.set(false)
    }

    fun snapshot(): TaxcomExportStatusResponse =
        TaxcomExportStatusResponse(
            status = status,
            running = running.get(),
            currentStage = currentStage,
            startedAt = lastStartedAt,
            finishedAt = lastFinishedAt,
            processedOutlets = processedOutlets.get(),
            processedKkt = processedKkt.get(),
            processedShifts = processedShifts.get(),
            processedReceipts = processedReceipts.get(),
            lastError = lastError,
        )

    companion object {
        private const val STATUS_IDLE = "IDLE"
        private const val STATUS_RUNNING = "RUNNING"
        private const val STATUS_COMPLETED = "COMPLETED"
        private const val STATUS_FAILED = "FAILED"
        private const val STAGE_STARTING = "STARTING"
    }
}
