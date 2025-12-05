package ru.sogaz.site.paymentReceiptService.service

import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.ReceiptSendRequest
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptCreateResponse

interface EventHandlerService {
    fun handleReceiptRequested(request: PaymentReceiptCreateRequest): PaymentReceiptCreateResponse

    fun handleReceiptCreated(receiptSendRequest: ReceiptSendRequest)
}
