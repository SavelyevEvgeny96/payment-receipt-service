package ru.sogaz.site.paymentReceiptService.taxcom.service

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.config.loggerFor
import ru.sogaz.site.paymentReceiptService.taxcom.config.TaxcomProperties
import ru.sogaz.site.paymentReceiptService.taxcom.repository.TaxcomJdbcRepository
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier

@Service
@ConditionalOnProperty(prefix = "taxcom.export", name = ["enabled"], havingValue = "true")
class TaxcomReceiptEnrichmentService(
    private val taxcomProperties: TaxcomProperties,
    private val taxcomJdbcRepository: TaxcomJdbcRepository,
    private val taxcomReceiptWorkerService: TaxcomReceiptWorkerService,
) {
    private val log = loggerFor(javaClass)

    fun startReceiptWorkers(discoveryFinished: AtomicBoolean, stopRequested: () -> Boolean): CompletableFuture<Long> {
        val workerCount = taxcomProperties.export.receiptWorkers.coerceAtLeast(MIN_RECEIPT_WORKERS)
        val threadNumber = AtomicInteger(0)
        val executor = Executors.newFixedThreadPool(workerCount) { task ->
            Thread(task, "taxcom-receipt-worker-${threadNumber.incrementAndGet()}")
        }
        val futures = (1..workerCount).map { workerIndex ->
            val workerId = "taxcom-receipt-worker-$workerIndex-${UUID.randomUUID()}"
            CompletableFuture.supplyAsync(
                Supplier { runWorker(workerId, discoveryFinished, stopRequested) },
                executor,
            )
        }

        return CompletableFuture
            .allOf(*futures.toTypedArray())
            .thenApply { futures.sumOf { it.join() } }
            .whenComplete { processed, ex ->
                if (ex == null) {
                    log.info("Taxcom export: receipt workers завершены, processed={}", processed)
                } else {
                    log.error("Taxcom export: receipt workers завершились с ошибкой", ex)
                }
                executor.shutdown()
            }
    }

    private fun runWorker(workerId: String, discoveryFinished: AtomicBoolean, stopRequested: () -> Boolean): Long {
        log.info("Taxcom export: worker={} стартовал", workerId)
        var processed = 0L
        while (true) {
            if (stopRequested()) {
                log.info("Taxcom export: worker={} получил команду остановки, processed={}", workerId, processed)
                return processed
            }
            val receipts = taxcomJdbcRepository.claimPendingReceipts(
                taxcomProperties.export.receiptClaimBatchSize.coerceAtLeast(MIN_RECEIPT_CLAIM_BATCH_SIZE),
                taxcomProperties.export.maxAttempts,
                taxcomProperties.export.receiptLockTimeoutMinutes.coerceAtLeast(MIN_RECEIPT_LOCK_TIMEOUT_MINUTES),
                workerId,
            )
            if (receipts.isEmpty()) {
                if (discoveryFinished.get()) {
                    log.info("Taxcom export: worker={} завершен, processed={}", workerId, processed)
                    return processed
                }
                Thread.sleep(taxcomProperties.export.receiptWorkerIdleDelayMs.coerceAtLeast(MIN_RECEIPT_IDLE_DELAY_MS))
                continue
            }

            log.info("Taxcom export: worker={} забрал чеков в работу: {}", workerId, receipts.size)
            receipts.forEach { receipt ->
                if (stopRequested()) {
                    log.info("Taxcom export: worker={} останавливается перед следующим чеком, processed={}", workerId, processed)
                    return processed
                }
                taxcomReceiptWorkerService.processReceipt(receipt, workerId)
                processed++
            }
        }
    }

    companion object {
        private const val MIN_RECEIPT_WORKERS = 1
        private const val MIN_RECEIPT_CLAIM_BATCH_SIZE = 1
        private const val MIN_RECEIPT_IDLE_DELAY_MS = 100L
        private const val MIN_RECEIPT_LOCK_TIMEOUT_MINUTES = 1
    }
}
