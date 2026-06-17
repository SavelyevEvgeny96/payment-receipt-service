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
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomReceiptRow
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomShiftRow
import ru.sogaz.site.paymentReceiptService.taxcom.repository.TaxcomJdbcRepository
import java.time.LocalDate
import java.util.UUID

@Service
@ConditionalOnProperty(prefix = "taxcom.job", name = ["enabled"], havingValue = "true")
class TaxcomExportService(
    private val taxcomClient: TaxcomClient,
    private val taxcomAuthService: TaxcomAuthService,
    private val taxcomJdbcRepository: TaxcomJdbcRepository,
    private val taxcomProperties: TaxcomProperties,
    private val taxcomExportJobState: TaxcomExportJobState,
) {
    private val log = loggerFor(javaClass)

    @Async
    fun runAsync() {
        if (!taxcomExportJobState.start()) {
            log.warn("Taxcom export: ручной запуск отклонен, выгрузка уже выполняется")
            return
        }
        runCatching { runInternal() }
            .onSuccess { taxcomExportJobState.finish(success = true) }
            .onFailure { ex ->
                log.error("Taxcom export: выгрузка завершилась с ошибкой", ex)
                taxcomExportJobState.finish(success = false, message = ex.message)
            }
    }

    fun status(): String = taxcomExportJobState.snapshot()

    private fun runInternal() {
        log.info("Taxcom export: старт ручной выгрузки")
        taxcomJdbcRepository.ensureSchema()
        loadOutlets()
        processPendingOutlets()
        processPendingKkt()
        processPendingShifts()
        processPendingReceipts()
        processPendingShifts()
        processPendingKkt()
        processPendingOutlets()
        log.info("Taxcom export: ручная выгрузка завершена")
    }

    @Transactional("taxcomTransactionManager")
    fun loadOutlets() {
        log.info("Taxcom export: загружаем список торговых точек")
        val response = taxcomAuthService.executeWithAuthRetry("OutletList") { token -> taxcomClient.getOutletList(token) }
        taxcomJdbcRepository.upsertOutlets(response.records)
        log.info("Taxcom export: список торговых точек сохранен, records={}", response.records.size)
    }

    fun processPendingOutlets() {
        val outlets = taxcomJdbcRepository.findPendingOutlets(taxcomProperties.job.batchSize)
        log.info("Taxcom export: найдено торговых точек для обработки: {}", outlets.size)
        outlets.forEach(::processOutlet)
    }

    fun processPendingKkt() {
        val kktList = taxcomJdbcRepository.findPendingKkt(taxcomProperties.job.batchSize)
        log.info("Taxcom export: найдено ККТ для обработки: {}", kktList.size)
        kktList.forEach(::processKkt)
    }

    fun processPendingShifts() {
        val shifts = taxcomJdbcRepository.findPendingShifts(taxcomProperties.job.batchSize)
        log.info("Taxcom export: найдено смен для обработки: {}", shifts.size)
        shifts.forEach(::processShift)
    }

    fun processPendingReceipts() {
        val receipts = taxcomJdbcRepository.findPendingReceipts(taxcomProperties.job.batchSize)
        log.info("Taxcom export: найдено чеков для догрузки DocumentInfo/DocumentURL: {}", receipts.size)
        receipts.forEach(::processReceipt)
    }

    @Transactional("taxcomTransactionManager")
    fun processOutlet(outlet: TaxcomOutletRow) {
        runCatching {
            log.info("Taxcom export: загружаем ККТ по торговой точке outletId={}, name={}", outlet.id, outlet.name)
            val response = taxcomAuthService.executeWithAuthRetry("KKTList") { token -> taxcomClient.getKktList(token, outlet.id) }
            if (response.records.isEmpty()) {
                log.info("Taxcom export: ККТ по торговой точке outletId={} не найдены, помечаем точку обработанной", outlet.id)
                taxcomJdbcRepository.markOutletUploaded(outlet.id)
                return
            }
            taxcomJdbcRepository.upsertKkt(outlet.id, response.records)
            taxcomJdbcRepository.markOutletUploadedIfDone(outlet.id)
            log.info("Taxcom export: ККТ по торговой точке outletId={} сохранены, records={}", outlet.id, response.records.size)
        }.onFailure { ex ->
            taxcomJdbcRepository.saveError("list_outlets", outlet.id, ex.message)
            log.error("Taxcom export: ошибка обработки торговой точки outletId={}", outlet.id, ex)
        }
    }

    @Transactional("taxcomTransactionManager")
    fun processKkt(kkt: TaxcomKktRow) {
        runCatching {
            log.info("Taxcom export: загружаем смены по ККТ kktId={}, fn={}", kkt.id, kkt.numFn)
            val response = taxcomAuthService.executeWithAuthRetry("ShiftList") { token ->
                taxcomClient.getShiftList(token, kkt.numFn, taxcomProperties.job.begin, LocalDate.now().atStartOfDay())
            }
            if (response.records.isEmpty()) {
                log.info("Taxcom export: смены по ККТ kktId={}, fn={} не найдены, помечаем ККТ обработанной", kkt.id, kkt.numFn)
                taxcomJdbcRepository.markKktUploaded(kkt.id)
                taxcomJdbcRepository.markOutletUploadedIfDone(kkt.outletId)
                return
            }
            taxcomJdbcRepository.upsertShifts(kkt.id, response.records)
            taxcomJdbcRepository.markKktUploadedIfDone(kkt.id)
            taxcomJdbcRepository.markOutletUploadedIfDone(kkt.outletId)
            log.info("Taxcom export: смены по ККТ kktId={}, fn={} сохранены, records={}", kkt.id, kkt.numFn, response.records.size)
        }.onFailure { ex ->
            taxcomJdbcRepository.saveError("list_kkt", kkt.id, ex.message)
            log.error("Taxcom export: ошибка обработки ККТ kktId={}, fn={}", kkt.id, kkt.numFn, ex)
        }
    }

    @Transactional("taxcomTransactionManager")
    fun processShift(shift: TaxcomShiftRow) {
        runCatching {
            log.info("Taxcom export: загружаем документы по смене shiftId={}, fn={}, shift={}", shift.id, shift.numFn, shift.num)
            val response = taxcomAuthService.executeWithAuthRetry("DocumentList") { token ->
                taxcomClient.getDocumentList(token, shift.numFn, shift.num)
            }
            if (response.records.isEmpty()) {
                log.info("Taxcom export: документы по смене shiftId={}, fn={}, shift={} не найдены, помечаем смену обработанной", shift.id, shift.numFn, shift.num)
                taxcomJdbcRepository.markShiftUploaded(shift.id)
                taxcomJdbcRepository.markKktUploadedIfDone(shift.kktId)
                return
            }
            taxcomJdbcRepository.upsertDocuments(shift.id, response.records)
            taxcomJdbcRepository.markShiftUploadedIfDone(shift.id)
            taxcomJdbcRepository.markKktUploadedIfDone(shift.kktId)
            log.info("Taxcom export: документы по смене shiftId={} сохранены, records={}", shift.id, response.records.size)
        }.onFailure { ex ->
            taxcomJdbcRepository.saveError("list_shifts", shift.id, ex.message)
            log.error("Taxcom export: ошибка обработки смены shiftId={}, fn={}, shift={}", shift.id, shift.numFn, shift.num, ex)
        }
    }

    @Transactional("taxcomTransactionManager")
    fun processReceipt(receipt: TaxcomReceiptRow) {
        runCatching {
            log.info("Taxcom export: догружаем чек receiptId={}, fn={}, fd={}", receipt.id, receipt.numFn, receipt.fdNumber)
            if (!receipt.documentInfoUploaded || !receipt.subjectsUploaded) {
                val response = taxcomAuthService.executeWithAuthRetry("DocumentInfo") { token ->
                    taxcomClient.getDocumentInfo(token, receipt.numFn, receipt.fdNumber)
                }
                taxcomJdbcRepository.updateDocumentInfo(receipt.id, response)
                log.info("Taxcom export: DocumentInfo сохранен receiptId={}, fn={}, fd={}", receipt.id, receipt.numFn, receipt.fdNumber)
            }
            if (!receipt.documentUrlUploaded) {
                val response = taxcomAuthService.executeWithAuthRetry("DocumentURL") { token ->
                    taxcomClient.getDocumentUrl(token, receipt.numFn, receipt.fdNumber)
                }
                taxcomJdbcRepository.updateDocumentUrl(receipt.id, response.taxcomReceiptUrl, response)
                log.info("Taxcom export: DocumentURL сохранен receiptId={}, fn={}, fd={}", receipt.id, receipt.numFn, receipt.fdNumber)
            }
            taxcomJdbcRepository.markReceiptUploadedIfDone(receipt.id)
            taxcomJdbcRepository.markShiftUploadedIfDone(receipt.shiftId)
        }.onFailure { ex ->
            taxcomJdbcRepository.saveError("receipts_taxcom", receipt.id, ex.message)
            log.error("Taxcom export: ошибка догрузки чека receiptId={}, fn={}, fd={}", receipt.id, receipt.numFn, receipt.fdNumber, ex)
        }
    }
}
