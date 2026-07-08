package ru.sogaz.site.paymentReceiptService.taxcom.service

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.config.loggerFor
import ru.sogaz.site.paymentReceiptService.taxcom.client.TaxcomClient
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomReceiptRow
import ru.sogaz.site.paymentReceiptService.taxcom.repository.TaxcomJdbcRepository

@Service
@ConditionalOnProperty(prefix = "taxcom.export", name = ["enabled"], havingValue = "true")
class TaxcomReceiptWorkerService(
    private val taxcomClient: TaxcomClient,
    private val taxcomAuthService: TaxcomAuthService,
    private val taxcomJdbcRepository: TaxcomJdbcRepository,
    private val taxcomExportRunState: TaxcomExportRunState,
) {
    private val log = loggerFor(javaClass)

    fun processReceipt(receipt: TaxcomReceiptRow, workerId: String) {
        runCatching {
            log.info("Taxcom export: worker={} догружает чек receiptId={}, fn={}, fd={}", workerId, receipt.id, receipt.numFn, receipt.fdNumber)
            if (!receipt.documentInfoUploaded || !receipt.subjectsUploaded) {
                val response = taxcomAuthService.executeWithAuthRetry("DocumentInfo") { token ->
                    taxcomClient.getDocumentInfo(token, receipt.numFn, receipt.fdNumber)
                }
                taxcomJdbcRepository.updateDocumentInfo(receipt.id, response)
                log.info("Taxcom export: worker={} DocumentInfo сохранен receiptId={}, fn={}, fd={}", workerId, receipt.id, receipt.numFn, receipt.fdNumber)
            }
            if (!receipt.documentUrlUploaded) {
                val response = taxcomAuthService.executeWithAuthRetry("DocumentURL") { token ->
                    taxcomClient.getDocumentUrl(token, receipt.numFn, receipt.fdNumber)
                }
                taxcomJdbcRepository.updateDocumentUrl(receipt.id, response.taxcomReceiptUrl, response)
                log.info("Taxcom export: worker={} DocumentURL сохранен receiptId={}, fn={}, fd={}", workerId, receipt.id, receipt.numFn, receipt.fdNumber)
            }
            taxcomJdbcRepository.markReceiptUploadedIfDone(receipt.id)
            taxcomJdbcRepository.releaseReceiptProcessing(receipt.id, workerId)
        }.onFailure { ex ->
            taxcomJdbcRepository.saveReceiptError(receipt.id, workerId, ex.message)
            log.error("Taxcom export: worker={} ошибка догрузки чека receiptId={}, fn={}, fd={}", workerId, receipt.id, receipt.numFn, receipt.fdNumber, ex)
        }
        taxcomExportRunState.receiptProcessed()
    }
}
