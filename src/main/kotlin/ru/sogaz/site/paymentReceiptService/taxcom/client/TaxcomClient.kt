package ru.sogaz.site.paymentReceiptService.taxcom.client

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomDocumentInfoResponse
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomDocumentListResponse
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomDocumentUrlResponse
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomKktListResponse
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomLoginRequest
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomLoginResponse
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomOutletListResponse
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomShiftListResponse
import java.time.LocalDateTime
import java.util.UUID

@ConditionalOnProperty(prefix = "taxcom.export", name = ["enabled"], havingValue = "true")
@FeignClient(
    name = "taxcom-client",
    url = "\${taxcom.api.base-url}",
)
interface TaxcomClient {
    @PostMapping(value = ["/API/v2/Login"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun login(
        @RequestHeader("Integrator-ID") integratorId: String,
        @RequestBody request: TaxcomLoginRequest,
    ): TaxcomLoginResponse

    @GetMapping(value = ["/API/v2/OutletList"])
    fun getOutletList(
        @RequestHeader("Session-Token") token: String,
    ): TaxcomOutletListResponse

    @GetMapping(value = ["/API/v2/KKTList"])
    fun getKktList(
        @RequestHeader("Session-Token") token: String,
        @RequestParam id: UUID,
    ): TaxcomKktListResponse

    @GetMapping(value = ["/API/v2/ShiftList"])
    fun getShiftList(
        @RequestHeader("Session-Token") token: String,
        @RequestParam fn: String,
        @RequestParam begin: LocalDateTime,
        @RequestParam end: LocalDateTime,
    ): TaxcomShiftListResponse

    @GetMapping(value = ["/API/v2/DocumentList"])
    fun getDocumentList(
        @RequestHeader("Session-Token") token: String,
        @RequestParam fn: String,
        @RequestParam shift: Int,
        @RequestParam type: List<Int> = DOCUMENT_TYPES,
    ): TaxcomDocumentListResponse

    @GetMapping(value = ["/API/v2/DocumentInfo"])
    fun getDocumentInfo(
        @RequestHeader("Session-Token") token: String,
        @RequestParam fn: String,
        @RequestParam fd: String,
    ): TaxcomDocumentInfoResponse

    @GetMapping(value = ["/API/v2/DocumentURL"])
    fun getDocumentUrl(
        @RequestHeader("Session-Token") token: String,
        @RequestParam fn: String,
        @RequestParam fd: String,
    ): TaxcomDocumentUrlResponse

    companion object {
        val DOCUMENT_TYPES = listOf(3, 31)
    }
}
