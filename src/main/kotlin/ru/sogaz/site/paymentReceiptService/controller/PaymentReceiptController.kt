package ru.sogaz.site.paymentReceiptService.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentReceiptService.mapper.receipt.ReceiptMapper
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptStatusRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptUpdateRequest
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptCreateResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptStatusResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptUpdateResponse
import ru.sogaz.site.paymentReceiptService.service.receipt.ReceiptService
import ru.sogaz.site.paymentReceiptService.service.receipt.ReceiptStatusService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse

@Validated
@RestController
@RequestMapping("/paymentcheck")
class PaymentReceiptController(
    private val receiptService: ReceiptService,
    private val receiptMapper: ReceiptMapper,
    private val receiptStatusService: ReceiptStatusService,
) {
    companion object {
        const val CREATE_RECEIPT_CODE_SUCCESS = 1101550200
        const val UPDATE_STATUS_CODE_SUCCESS = 1101560200
        const val GET_STATUS_CODE_SUCCESS = 1101520200
    }

    @PostMapping("/create")
    fun createPaymentCheck(
        @Valid @RequestBody request: PaymentReceiptCreateRequest,
    ): ResponseEntity<Response<PaymentReceiptCreateResponse>> =
        request
            .run(receiptMapper::fromCreateRequest)
            .run(receiptService::createReceipt)
            .wrapToSuccessResponse(CREATE_RECEIPT_CODE_SUCCESS)
            .wrapToOkResponseEntity()

    @PostMapping("/status")
    fun updatePaymentStatus(
        @RequestBody request: PaymentReceiptStatusRequest,
    ): ResponseEntity<Response<PaymentReceiptStatusResponse>> =
        request
            .run(receiptStatusService::setStatus)
            .wrapToSuccessResponse(UPDATE_STATUS_CODE_SUCCESS)
            .wrapToOkResponseEntity()

    @PatchMapping("/statusupdate")
    fun getPaymentStatus(
        @RequestBody request: PaymentReceiptUpdateRequest,
    ): ResponseEntity<Response<PaymentReceiptUpdateResponse>> =
        request
            .run(receiptStatusService::updateStatusFromAtol)
            .wrapToSuccessResponse(GET_STATUS_CODE_SUCCESS)
            .wrapToOkResponseEntity()

    private fun <T> T.wrapToSuccessResponse(code: Int) = getSuccessResponse(getTraceId(), code, this)

    private fun <T> T.wrapToOkResponseEntity() = ResponseEntity.ok(this)
}
