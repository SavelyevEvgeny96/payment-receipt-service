package ru.sogaz.site.paymentReceiptService.taxcom.client

import feign.CollectionFormat
import org.springframework.cloud.openfeign.CollectionFormat as FeignCollectionFormat
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import ru.sogaz.site.paymentReceiptService.taxcom.config.TaxcomFeignConfig
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomDocumentInfoResponse
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomDocumentListResponse
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomDocumentUrlResponse
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomKktListResponse
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomLoginRequest
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomLoginResponse
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomOutletListResponse
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomShiftListResponse
import java.util.UUID

@FeignClient(
    name = "taxcom-client",
    url = "\${taxcom.api.base-url}",
    configuration = [TaxcomFeignConfig::class],
)
interface TaxcomClient {
    @PostMapping(
        value = ["/API/v2/Login"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        headers = ["Content-Type=application/json", "Accept=application/json"],
    )
    fun login(
        @RequestHeader("Integrator-ID") integratorId: String,
        @RequestBody request: TaxcomLoginRequest,
    ): TaxcomLoginResponse

    @GetMapping(value = ["/API/v2/OutletList"])
    fun getOutletList(
        @RequestHeader("Session-Token") token: String,
        @RequestParam pn: Int,
        @RequestParam ps: Int,
    ): TaxcomOutletListResponse

    @GetMapping(value = ["/API/v2/KKTList"])
    fun getKktList(
        @RequestHeader("Session-Token") token: String,
        @RequestParam id: UUID,
        @RequestParam pn: Int,
        @RequestParam ps: Int,
    ): TaxcomKktListResponse

    @GetMapping(value = ["/API/v2/ShiftList"])
    fun getShiftList(
        @RequestHeader("Session-Token") token: String,
        @RequestParam fn: String,
        @RequestParam begin: String,
        @RequestParam end: String,
        @RequestParam pn: Int,
        @RequestParam ps: Int,
    ): TaxcomShiftListResponse

    @FeignCollectionFormat(CollectionFormat.EXPLODED)
    @GetMapping(value = ["/API/v2/DocumentList"])
    fun getDocumentList(
        @RequestHeader("Session-Token") token: String,
        @RequestParam fn: String,
        @RequestParam shift: Int,
        @RequestParam pn: Int,
        @RequestParam ps: Int,
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
