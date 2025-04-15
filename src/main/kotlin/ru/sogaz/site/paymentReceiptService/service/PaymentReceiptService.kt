package ru.sogaz.site.paymentReceiptService.service

import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptStatusRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptUpdateRequest
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptCreateResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptStatusResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptUpdateResponse
import ru.sogaz.siter.models.resonses.Response

interface PaymentReceiptService {
    fun createCheck(request: PaymentReceiptCreateRequest): Response<PaymentReceiptCreateResponse>

    fun updateStatus(request: PaymentReceiptStatusRequest): Response<PaymentReceiptStatusResponse>

    fun getStatus(request: PaymentReceiptUpdateRequest): Response<PaymentReceiptUpdateResponse>
}
