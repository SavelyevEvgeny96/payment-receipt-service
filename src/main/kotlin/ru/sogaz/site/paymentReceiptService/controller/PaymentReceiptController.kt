package ru.sogaz.site.paymentReceiptService.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptStatusRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptUpdateRequest
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptCreateResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptStatusResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptUpdateResponse
import ru.sogaz.site.paymentReceiptService.service.PaymentReceiptService
import ru.sogaz.site.paymentReceiptService.validation.PaymentReceiptCreateRequestValidation
import ru.sogaz.siter.models.resonses.Response

@RestController
@RequestMapping("/paymentCheck")
class PaymentReceiptController(
    private val paymentReceiptService: PaymentReceiptService,
    private val paymentReceiptCreateRequestValidation: PaymentReceiptCreateRequestValidation,
) {
    @PostMapping("/create")
    fun createPaymentCheck(
        @RequestBody request: PaymentReceiptCreateRequest,
    ): ResponseEntity<Response<PaymentReceiptCreateResponse>> {
        paymentReceiptCreateRequestValidation.isValid(request)
        return ResponseEntity.ok(paymentReceiptService.createReceipt(request))
    }

    @PostMapping("/status")
    fun updatePaymentStatus(
        @RequestBody request: PaymentReceiptStatusRequest,
    ): ResponseEntity<Response<PaymentReceiptStatusResponse>> = ResponseEntity.ok(paymentReceiptService.updateStatus(request))

    @PatchMapping("/statusUpdate")
    fun getPaymentStatus(
        @RequestBody request: PaymentReceiptUpdateRequest,
    ): ResponseEntity<Response<PaymentReceiptUpdateResponse>> = ResponseEntity.ok(paymentReceiptService.getStatus(request))
}
