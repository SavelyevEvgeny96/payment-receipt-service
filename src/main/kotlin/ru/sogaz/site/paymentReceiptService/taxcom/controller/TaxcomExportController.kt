package ru.sogaz.site.paymentReceiptService.taxcom.controller

import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomExportStatusResponse
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomRunResponse
import ru.sogaz.site.paymentReceiptService.taxcom.service.TaxcomExportService

@RestController
@RequestMapping("/internal/taxcom-export")
class TaxcomExportController(
    private val taxcomExportServiceProvider: ObjectProvider<TaxcomExportService>,
    @Value("\${taxcom.export.enabled:false}")
    private val taxcomExportEnabled: Boolean,
) {
    @PostMapping("/run")
    fun run(): ResponseEntity<TaxcomRunResponse> {
        val taxcomExportService = getEnabledService() ?: return exportDisabledResponse()
        taxcomExportService.runAsync()
        return ResponseEntity.accepted().body(TaxcomRunResponse(accepted = true, status = taxcomExportService.status()))
    }

    @GetMapping("/status")
    fun status(): ResponseEntity<TaxcomRunResponse> {
        val taxcomExportService = getEnabledService()
        return if (taxcomExportService == null) {
            exportDisabledResponse()
        } else {
            ResponseEntity.ok(TaxcomRunResponse(accepted = false, status = taxcomExportService.status()))
        }
    }

    private fun getEnabledService(): TaxcomExportService? =
        taxcomExportServiceProvider.getIfAvailable().takeIf { taxcomExportEnabled }

    private fun exportDisabledResponse(): ResponseEntity<TaxcomRunResponse> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(TaxcomRunResponse(accepted = false, status = TaxcomExportStatusResponse.disabled()))
}
