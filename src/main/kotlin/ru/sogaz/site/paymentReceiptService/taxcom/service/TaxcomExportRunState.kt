package ru.sogaz.site.paymentReceiptService.taxcom.service

import org.springframework.stereotype.Component
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomExportStatusResponse
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomProcessStatusResponse
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

@Component
class TaxcomExportRunState {
    private val discovery = ProcessState()
    private val receipts = ProcessState()

    fun startDiscovery(): Boolean = discovery.start()

    fun startReceipts(): Boolean = receipts.start()

    fun discoveryStage(stage: String) = discovery.stage(stage)

    fun receiptStage(stage: String) = receipts.stage(stage)

    fun outletProcessed() = discovery.processedOutlets.incrementAndGet()

    fun kktProcessed() = discovery.processedKkt.incrementAndGet()

    fun shiftProcessed() = discovery.processedShifts.incrementAndGet()

    fun receiptProcessed() = receipts.processedReceipts.incrementAndGet()

    fun finishDiscovery(success: Boolean, message: String? = null) = discovery.finish(success, message)

    fun finishReceipts(success: Boolean, message: String? = null) = receipts.finish(success, message)

    fun requestDiscoveryStop(): Boolean = discovery.requestStop()

    fun requestReceiptsStop(): Boolean = receipts.requestStop()

    fun requestStopAll(): Boolean = requestDiscoveryStop() || requestReceiptsStop()

    fun isDiscoveryStopRequested(): Boolean = discovery.stopRequested.get()

    fun isReceiptsStopRequested(): Boolean = receipts.stopRequested.get()

    fun snapshot(): TaxcomExportStatusResponse {
        val discoverySnapshot = discovery.snapshot()
        val receiptsSnapshot = receipts.snapshot()
        val running = discoverySnapshot.running || receiptsSnapshot.running
        val status = when {
            discoverySnapshot.running || receiptsSnapshot.running -> STATUS_RUNNING
            discoverySnapshot.status == STATUS_FAILED || receiptsSnapshot.status == STATUS_FAILED -> STATUS_FAILED
            discoverySnapshot.status == STATUS_STOPPED || receiptsSnapshot.status == STATUS_STOPPED -> STATUS_STOPPED
            discoverySnapshot.status == STATUS_COMPLETED || receiptsSnapshot.status == STATUS_COMPLETED -> STATUS_COMPLETED
            else -> STATUS_IDLE
        }
        val currentStage = listOfNotNull(discoverySnapshot.currentStage, receiptsSnapshot.currentStage)
            .joinToString("+")
            .takeIf { it.isNotBlank() }
        return TaxcomExportStatusResponse(
            status = status,
            running = running,
            currentStage = currentStage,
            startedAt = listOfNotNull(discoverySnapshot.startedAt, receiptsSnapshot.startedAt).minOrNull(),
            finishedAt = listOfNotNull(discoverySnapshot.finishedAt, receiptsSnapshot.finishedAt).maxOrNull(),
            processedOutlets = discoverySnapshot.processedOutlets,
            processedKkt = discoverySnapshot.processedKkt,
            processedShifts = discoverySnapshot.processedShifts,
            processedReceipts = receiptsSnapshot.processedReceipts,
            lastError = discoverySnapshot.lastError ?: receiptsSnapshot.lastError,
            discovery = discoverySnapshot,
            receipts = receiptsSnapshot,
        )
    }

    private class ProcessState {
        val running = AtomicBoolean(false)
        val stopRequested = AtomicBoolean(false)
        val processedOutlets = AtomicLong(0)
        val processedKkt = AtomicLong(0)
        val processedShifts = AtomicLong(0)
        val processedReceipts = AtomicLong(0)

        @Volatile
        var status: String = STATUS_IDLE

        @Volatile
        var currentStage: String? = null

        @Volatile
        var lastError: String? = null

        @Volatile
        var lastStartedAt: Instant? = null

        @Volatile
        var lastFinishedAt: Instant? = null

        fun start(): Boolean =
            running.compareAndSet(false, true).also { started ->
                if (started) {
                    stopRequested.set(false)
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

        fun requestStop(): Boolean = running.get().also { isRunning ->
            if (isRunning) {
                stopRequested.set(true)
                status = STATUS_STOPPING
            }
        }

        fun finish(success: Boolean, message: String? = null) {
            val stopped = stopRequested.get()
            lastFinishedAt = Instant.now()
            currentStage = null
            lastError = message
            status = when {
                stopped -> STATUS_STOPPED
                success -> STATUS_COMPLETED
                else -> STATUS_FAILED
            }
            running.set(false)
            stopRequested.set(false)
        }

        fun snapshot(): TaxcomProcessStatusResponse =
            TaxcomProcessStatusResponse(
                status = status,
                running = running.get(),
                stopRequested = stopRequested.get(),
                currentStage = currentStage,
                startedAt = lastStartedAt,
                finishedAt = lastFinishedAt,
                processedOutlets = processedOutlets.get(),
                processedKkt = processedKkt.get(),
                processedShifts = processedShifts.get(),
                processedReceipts = processedReceipts.get(),
                lastError = lastError,
            )
    }

    companion object {
        private const val STATUS_IDLE = "IDLE"
        private const val STATUS_RUNNING = "RUNNING"
        private const val STATUS_STOPPING = "STOPPING"
        private const val STATUS_STOPPED = "STOPPED"
        private const val STATUS_COMPLETED = "COMPLETED"
        private const val STATUS_FAILED = "FAILED"
        private const val STAGE_STARTING = "STARTING"
    }
}
