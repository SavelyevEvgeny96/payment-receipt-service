package ru.sogaz.site.paymentReceiptService.service.receipt

import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.event.ReceiptCreatedEvent
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest

interface ReceiptService {
    fun createReceipt(receiptCreateRequest: PaymentReceiptCreateRequest): Receipt

    fun sendReceipt(receiptCreatedEvent: ReceiptCreatedEvent): Receipt
}
