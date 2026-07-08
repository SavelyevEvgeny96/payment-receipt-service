package ru.sogaz.site.paymentReceiptService.taxcom.service

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.exceptionStarter.starter.config.loggerFor
import ru.sogaz.site.paymentReceiptService.taxcom.client.TaxcomClient
import ru.sogaz.site.paymentReceiptService.taxcom.config.TaxcomProperties
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomKktRow
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomOutletRow
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomShiftRow
import ru.sogaz.site.paymentReceiptService.taxcom.repository.TaxcomJdbcRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private class TaxcomExportStoppedException(message: String) : RuntimeException(message)

@Service
@ConditionalOnProperty(prefix = "taxcom.export", name = ["enabled"], havingValue = "true")
class TaxcomExportService(
    private val taxcomClient: TaxcomClient,
    private val taxcomAuthService: TaxcomAuthService,
    private val taxcomJdbcRepository: TaxcomJdbcRepository,
    private val taxcomProperties: TaxcomProperties,
    private val taxcomExportRunState: TaxcomExportRunState,
    private val taxcomReceiptEnrichmentService: TaxcomReceiptEnrichmentService,
) {
    private val log = loggerFor(javaClass)

    @Async
    fun runAsync() {
        if (!taxcomExportRunState.startDiscovery()) {
            log.warn("Taxcom export: полный запуск отклонен, discovery уже выполняется")
            return
        }
        if (!taxcomExportRunState.startReceipts()) {
            taxcomExportRunState.finishDiscovery(success = false, message = "Receipt enrichment is already running")
            log.warn("Taxcom export: полный запуск отклонен, receipt enrichment уже выполняется")
            return
        }
        runCatching { runInternal(startReceiptWorkers = true) }
            .onSuccess {
                taxcomExportRunState.finishDiscovery(success = true)
            }
            .onFailure { ex ->
                log.error("Taxcom export: выгрузка завершилась с ошибкой", ex)
                taxcomExportRunState.finishDiscovery(success = false, message = ex.message)
                taxcomExportRunState.finishReceipts(success = false, message = ex.message)
            }
    }

    @Async
    fun runDiscoveryAsync() {
        if (!taxcomExportRunState.startDiscovery()) {
            log.warn("Taxcom export: запуск discovery отклонен, discovery уже выполняется")
            return
        }
        runCatching { runInternal(startReceiptWorkers = false) }
            .onSuccess { taxcomExportRunState.finishDiscovery(success = true) }
            .onFailure { ex ->
                log.error("Taxcom export: discovery завершился с ошибкой", ex)
                taxcomExportRunState.finishDiscovery(success = false, message = ex.message)
            }
    }

    @Async
    fun runReceiptsAsync() {
        if (!taxcomExportRunState.startReceipts()) {
            log.warn("Taxcom export: запуск receipt enrichment отклонен, receipt enrichment уже выполняется")
            return
        }
        runCatching { runReceiptWorkersStandalone() }
            .onSuccess { taxcomExportRunState.finishReceipts(success = true) }
            .onFailure { ex ->
                log.error("Taxcom export: receipt enrichment завершился с ошибкой", ex)
                taxcomExportRunState.finishReceipts(success = false, message = ex.message)
            }
    }

    fun stopDiscovery(): Boolean = taxcomExportRunState.requestDiscoveryStop()

    fun stopReceipts(): Boolean = taxcomExportRunState.requestReceiptsStop()

    fun stopAll(): Boolean = taxcomExportRunState.requestStopAll()

    fun status() = taxcomExportRunState.snapshot()

    private fun runInternal(startReceiptWorkers: Boolean) {
        log.info("Taxcom export: старт discovery, startReceiptWorkers={}", startReceiptWorkers)
        taxcomExportRunState.discoveryStage(STAGE_SCHEMA)
        ensureDiscoveryNotStopped()
        taxcomJdbcRepository.ensureSchema()
        taxcomExportRunState.discoveryStage(STAGE_LOAD_OUTLETS)
        ensureDiscoveryNotStopped()
        loadOutlets()

        val discoveryFinished = AtomicBoolean(false)
        val receiptWorkersFuture = if (startReceiptWorkers) {
            taxcomReceiptEnrichmentService.startReceiptWorkers(discoveryFinished) { taxcomExportRunState.isReceiptsStopRequested() }
        } else {
            null
        }
        var discoveryError: Throwable? = null

        var cycle = FIRST_EXPORT_CYCLE
        try {
            while (!taxcomExportRunState.isDiscoveryStopRequested()) {
                log.info("Taxcom export: старт discovery-цикла cycle={}", cycle)
                var processedInCycle = 0

                taxcomExportRunState.discoveryStage(STAGE_PROCESS_OUTLETS)
                processedInCycle += processPendingOutlets()
                ensureDiscoveryNotStopped()
                taxcomExportRunState.discoveryStage(STAGE_PROCESS_KKT)
                processedInCycle += processPendingKkt()
                ensureDiscoveryNotStopped()
                taxcomExportRunState.discoveryStage(STAGE_PROCESS_SHIFTS)
                processedInCycle += processPendingShifts()

                log.info("Taxcom export: завершен discovery-цикл cycle={}, processed={}", cycle, processedInCycle)
                if (processedInCycle == 0) {
                    break
                }
                cycle++
            }
        } catch (ex: Throwable) {
            discoveryError = ex
        } finally {
            discoveryFinished.set(true)
        }

        if (receiptWorkersFuture != null) {
            taxcomExportRunState.receiptStage(STAGE_PROCESS_RECEIPTS)
            val processedReceipts = waitReceiptWorkers(receiptWorkersFuture)
            log.info("Taxcom export: receipt workers догрузили чеков: {}", processedReceipts)
            taxcomExportRunState.finishReceipts(success = discoveryError == null, message = discoveryError?.message)
        }
        discoveryError?.let { throw it }
        log.info("Taxcom export: discovery завершен")
    }

    private fun runReceiptWorkersStandalone() {
        log.info("Taxcom export: старт standalone receipt enrichment")
        taxcomExportRunState.receiptStage(STAGE_PROCESS_RECEIPTS)
        val receiptWorkersFuture = taxcomReceiptEnrichmentService.startReceiptWorkers(AtomicBoolean(true)) {
            taxcomExportRunState.isReceiptsStopRequested()
        }
        val processedReceipts = waitReceiptWorkers(receiptWorkersFuture)
        log.info("Taxcom export: standalone receipt enrichment завершен, processed={}", processedReceipts)
    }

    private fun ensureDiscoveryNotStopped() {
        if (taxcomExportRunState.isDiscoveryStopRequested()) {
            throw TaxcomExportStoppedException("Taxcom discovery stop requested")
        }
    }

    private fun waitReceiptWorkers(receiptWorkersFuture: java.util.concurrent.CompletableFuture<Long>): Long =
        try {
            receiptWorkersFuture.get()
        } catch (ex: ExecutionException) {
            throw ex.cause ?: ex
        }

    @Transactional("taxcomTransactionManager")
    fun loadOutlets() {
        log.info("Taxcom export: загружаем список торговых точек")
        val pageSize = taxcomProperties.export.listPageSize.coerceIn(MIN_LIST_PAGE_SIZE, MAX_LIST_PAGE_SIZE)
        var pageNumber = FIRST_LIST_PAGE
        var readRecords = 0
        var totalRecords: Int? = null

        while (true) {
            ensureDiscoveryNotStopped()
            val response = taxcomAuthService.executeWithAuthRetry("OutletList") { token ->
                taxcomClient.getOutletList(token, pageNumber, pageSize)
            }
            val records = response.records
            val counts = response.counts
            if (totalRecords == null) {
                totalRecords = counts?.recordFilteredCount?.takeIf { it > 0 } ?: counts?.recordCount
            }
            taxcomJdbcRepository.insertOutletsIfMissing(records)
            readRecords += records.size
            log.info(
                "Taxcom export: страница OutletList сохранена page={}, pageSize={}, records={}, read={}, total={}",
                pageNumber,
                pageSize,
                records.size,
                readRecords,
                totalRecords,
            )
            if (shouldStopPaging(records, readRecords, totalRecords, pageSize)) {
                break
            }
            pageNumber++
        }

        log.info("Taxcom export: список торговых точек сохранен, records={}", readRecords)
    }

    fun processPendingOutlets(): Int {
        val outlets = taxcomJdbcRepository.findPendingOutlets(taxcomProperties.export.batchSize, taxcomProperties.export.maxAttempts)
        log.info("Taxcom export: найдено торговых точек для обработки: {}", outlets.size)
        outlets.takeWhile { !taxcomExportRunState.isDiscoveryStopRequested() }.forEach(::processOutlet)
        return outlets.size
    }

    fun processPendingKkt(): Int {
        val kktList = taxcomJdbcRepository.findPendingKkt(taxcomProperties.export.batchSize, taxcomProperties.export.maxAttempts)
        log.info("Taxcom export: найдено ККТ для обработки: {}", kktList.size)
        return processKktBatch(kktList)
    }

    private fun processKktBatch(kktList: List<TaxcomKktRow>): Int {
        if (kktList.isEmpty()) {
            return 0
        }
        val workerCount = taxcomProperties.export.shiftListWorkers.coerceAtLeast(MIN_SHIFT_LIST_WORKERS)
        if (workerCount == MIN_SHIFT_LIST_WORKERS) {
            kktList.takeWhile { !taxcomExportRunState.isDiscoveryStopRequested() }.forEach(::processKkt)
            return kktList.size
        }

        val executor = Executors.newFixedThreadPool(workerCount)
        return try {
            val futures = kktList.map { kkt ->
                executor.submit<Int> {
                    if (taxcomExportRunState.isDiscoveryStopRequested()) {
                        0
                    } else {
                        processKkt(kkt)
                        1
                    }
                }
            }
            futures.sumOf { future -> future.get() }
        } finally {
            executor.shutdown()
        }
    }

    fun processPendingShifts(): Int {
        val shifts = taxcomJdbcRepository.findPendingShifts(taxcomProperties.export.batchSize, taxcomProperties.export.maxAttempts)
        log.info("Taxcom export: найдено смен для обработки: {}", shifts.size)
        shifts.takeWhile { !taxcomExportRunState.isDiscoveryStopRequested() }.forEach(::processShift)
        return shifts.size
    }

    @Transactional("taxcomTransactionManager")
    fun processOutlet(outlet: TaxcomOutletRow) {
        runCatching {
            log.info("Taxcom export: загружаем ККТ по торговой точке outletId={}, name={}", outlet.id, outlet.name)
            val readRecords = loadOutletKkt(outlet)
            if (readRecords == 0) {
                log.info("Taxcom export: ККТ по торговой точке outletId={} не найдены, помечаем точку обработанной", outlet.id)
                taxcomJdbcRepository.markOutletUploaded(outlet.id)
                return@runCatching
            }
            taxcomJdbcRepository.markOutletUploaded(outlet.id)
            log.info("Taxcom export: ККТ по торговой точке outletId={} сохранены, records={}", outlet.id, readRecords)
        }.onFailure { ex ->
            if (ex is TaxcomExportStoppedException) throw ex
            taxcomJdbcRepository.saveError("list_outlets", outlet.id, ex.message)
            log.error("Taxcom export: ошибка обработки торговой точки outletId={}", outlet.id, ex)
        }
        taxcomExportRunState.outletProcessed()
    }

    private fun loadOutletKkt(outlet: TaxcomOutletRow): Int {
        val pageSize = taxcomProperties.export.listPageSize.coerceIn(MIN_LIST_PAGE_SIZE, MAX_LIST_PAGE_SIZE)
        var pageNumber = FIRST_LIST_PAGE
        var readRecords = 0
        var totalRecords: Int? = null

        while (true) {
            ensureDiscoveryNotStopped()
            val response = taxcomAuthService.executeWithAuthRetry("KKTList") { token ->
                taxcomClient.getKktList(token, outlet.id, pageNumber, pageSize)
            }
            val records = response.records
            val counts = response.counts
            if (totalRecords == null) {
                totalRecords = counts?.recordFilteredCount?.takeIf { it > 0 } ?: counts?.recordCount
            }
            taxcomJdbcRepository.insertKktIfMissing(outlet.id, records)
            readRecords += records.size
            log.info(
                "Taxcom export: страница KKTList сохранена outletId={}, page={}, pageSize={}, records={}, read={}, total={}",
                outlet.id,
                pageNumber,
                pageSize,
                records.size,
                readRecords,
                totalRecords,
            )
            if (shouldStopPaging(records, readRecords, totalRecords, pageSize)) {
                break
            }
            pageNumber++
        }

        return readRecords
    }

    @Transactional("taxcomTransactionManager")
    fun processKkt(kkt: TaxcomKktRow) {
        runCatching {
            log.info("Taxcom export: загружаем смены по ККТ kktId={}, fn={}", kkt.id, kkt.numFn)
            val readRecords = loadKktShifts(kkt)
            if (readRecords == 0) {
                log.info("Taxcom export: смены по ККТ kktId={}, fn={} не найдены, помечаем ККТ обработанной", kkt.id, kkt.numFn)
                taxcomJdbcRepository.markKktUploaded(kkt.id)
                taxcomJdbcRepository.markOutletUploaded(kkt.outletId)
                return@runCatching
            }
            taxcomJdbcRepository.markKktUploaded(kkt.id)
            taxcomJdbcRepository.markOutletUploaded(kkt.outletId)
            log.info("Taxcom export: смены по ККТ kktId={}, fn={} сохранены, records={}", kkt.id, kkt.numFn, readRecords)
        }.onFailure { ex ->
            if (ex is TaxcomExportStoppedException) throw ex
            taxcomJdbcRepository.saveError("list_kkt", kkt.id, ex.message)
            log.error("Taxcom export: ошибка обработки ККТ kktId={}, fn={}", kkt.id, kkt.numFn, ex)
        }
        taxcomExportRunState.kktProcessed()
    }

    private fun loadKktShifts(kkt: TaxcomKktRow): Int {
        val pageSize = taxcomProperties.export.listPageSize.coerceIn(MIN_LIST_PAGE_SIZE, MAX_LIST_PAGE_SIZE)
        var pageNumber = FIRST_LIST_PAGE
        var readRecords = 0
        var totalRecords: Int? = null

        while (true) {
            ensureDiscoveryNotStopped()
            val response = taxcomAuthService.executeWithAuthRetry("ShiftList") { token ->
                taxcomClient.getShiftList(
                    token,
                    kkt.numFn,
                    taxcomProperties.export.begin.toTaxcomDateTime(),
                    LocalDate.now().atStartOfDay().toTaxcomDateTime(),
                    pageNumber,
                    pageSize,
                )
            }
            val records = response.records
            val counts = response.counts
            if (totalRecords == null) {
                totalRecords = counts?.recordFilteredCount?.takeIf { it > 0 } ?: counts?.recordCount
            }
            taxcomJdbcRepository.insertShiftsIfMissing(kkt.id, records)
            readRecords += records.size
            log.info(
                "Taxcom export: страница ShiftList сохранена kktId={}, fn={}, page={}, pageSize={}, records={}, read={}, total={}",
                kkt.id,
                kkt.numFn,
                pageNumber,
                pageSize,
                records.size,
                readRecords,
                totalRecords,
            )
            if (shouldStopPaging(records, readRecords, totalRecords, pageSize)) {
                break
            }
            pageNumber++
        }

        return readRecords
    }

    @Transactional("taxcomTransactionManager")
    fun processShift(shift: TaxcomShiftRow) {
        runCatching {
            log.info("Taxcom export: загружаем документы по смене shiftId={}, fn={}, shift={}", shift.id, shift.numFn, shift.num)
            val readRecords = loadShiftDocuments(shift)
            if (readRecords == 0) {
                log.info("Taxcom export: документы по смене shiftId={}, fn={}, shift={} не найдены, помечаем смену обработанной", shift.id, shift.numFn, shift.num)
                taxcomJdbcRepository.markShiftUploaded(shift.id)
                taxcomJdbcRepository.markKktUploaded(shift.kktId)
                return@runCatching
            }
            taxcomJdbcRepository.markShiftUploaded(shift.id)
            taxcomJdbcRepository.markKktUploaded(shift.kktId)
            log.info("Taxcom export: документы по смене shiftId={} прочитаны из DocumentList, records={}", shift.id, readRecords)
        }.onFailure { ex ->
            if (ex is TaxcomExportStoppedException) throw ex
            taxcomJdbcRepository.saveError("list_shifts", shift.id, ex.message)
            log.error("Taxcom export: ошибка обработки смены shiftId={}, fn={}, shift={}", shift.id, shift.numFn, shift.num, ex)
        }
        taxcomExportRunState.shiftProcessed()
    }

    private fun loadShiftDocuments(shift: TaxcomShiftRow): Int {
        val pageSize = taxcomProperties.export.documentPageSize.coerceIn(MIN_DOCUMENT_PAGE_SIZE, MAX_DOCUMENT_PAGE_SIZE)
        var pageNumber = FIRST_DOCUMENT_PAGE
        var readRecords = 0
        var totalRecords: Int? = null

        while (true) {
            ensureDiscoveryNotStopped()
            val response = taxcomAuthService.executeWithAuthRetry("DocumentList") { token ->
                taxcomClient.getDocumentList(token, shift.numFn, shift.num, pageNumber, pageSize)
            }
            val records = response.records
            val counts = response.counts
            if (totalRecords == null) {
                totalRecords = counts?.recordFilteredCount?.takeIf { it > 0 } ?: counts?.recordCount
            }
            taxcomJdbcRepository.insertDocumentsIfMissing(shift.id, records)
            readRecords += records.size
            log.info(
                "Taxcom export: страница DocumentList сохранена shiftId={}, fn={}, shift={}, page={}, pageSize={}, records={}, read={}, total={}",
                shift.id,
                shift.numFn,
                shift.num,
                pageNumber,
                pageSize,
                records.size,
                readRecords,
                totalRecords,
            )
            if (shouldStopPaging(records, readRecords, totalRecords, pageSize)) {
                break
            }
            pageNumber++
        }

        return readRecords
    }

    private fun LocalDateTime.toTaxcomDateTime(): String = format(TAXCOM_DATE_TIME_FORMATTER)

    private fun shouldStopPaging(records: List<*>, readRecords: Int, totalRecords: Int?, pageSize: Int): Boolean =
        records.isEmpty() ||
            (totalRecords != null && readRecords >= totalRecords) ||
            (totalRecords == null && records.size < pageSize)

    companion object {
        private val TAXCOM_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        private const val FIRST_EXPORT_CYCLE = 1
        private const val FIRST_LIST_PAGE = 1
        private const val FIRST_DOCUMENT_PAGE = 1
        private const val MIN_LIST_PAGE_SIZE = 1
        private const val MAX_LIST_PAGE_SIZE = 1500
        private const val MIN_DOCUMENT_PAGE_SIZE = 1
        private const val MAX_DOCUMENT_PAGE_SIZE = 1500
        private const val MIN_SHIFT_LIST_WORKERS = 1

        private const val STAGE_SCHEMA = "SCHEMA"
        private const val STAGE_LOAD_OUTLETS = "LOAD_OUTLETS"
        private const val STAGE_PROCESS_OUTLETS = "PROCESS_OUTLETS"
        private const val STAGE_PROCESS_KKT = "PROCESS_KKT"
        private const val STAGE_PROCESS_SHIFTS = "PROCESS_SHIFTS"
        private const val STAGE_PROCESS_RECEIPTS = "PROCESS_RECEIPTS"
    }
}
