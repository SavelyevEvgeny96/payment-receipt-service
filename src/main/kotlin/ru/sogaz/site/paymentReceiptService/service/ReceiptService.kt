package ru.sogaz.site.paymentReceiptService.service

import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.event.ReceiptCreatedEvent
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptCreateResponse

interface ReceiptService {
    fun createReceipt(receipt: Receipt): PaymentReceiptCreateResponse

    fun sendReceipt(receiptCreatedEvent: ReceiptCreatedEvent): Receipt
}
