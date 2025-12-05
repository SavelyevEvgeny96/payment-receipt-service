package ru.sogaz.site.paymentReceiptService.producer

import ru.sogaz.site.paymentReceiptService.model.entity.Receipt

interface ReceiptEventsProducer {
    fun receiptSentEvent(receipt: Receipt)

    fun receiptCreatedEvent(receipt: Receipt)
}
