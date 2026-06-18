package ru.sogaz.site.paymentReceiptService.taxcom.controller

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomRunResponse
import ru.sogaz.site.paymentReceiptService.taxcom.service.TaxcomExportService

@RestController
@RequestMapping("/internal/taxcom-export")
@ConditionalOnProperty(prefix = "taxcom.export", name = ["enabled"], havingValue = "true")
class TaxcomExportController(
    private val taxcomExportService: TaxcomExportService,
) {
    @PostMapping("/run")
    fun run(): ResponseEntity<TaxcomRunResponse> {
        taxcomExportService.runAsync()
        return ResponseEntity.accepted().body(TaxcomRunResponse(accepted = true, status = taxcomExportService.status()))
    }

    @GetMapping("/status")
    fun status(): ResponseEntity<TaxcomRunResponse> =
        ResponseEntity.ok(TaxcomRunResponse(accepted = false, status = taxcomExportService.status()))
}
