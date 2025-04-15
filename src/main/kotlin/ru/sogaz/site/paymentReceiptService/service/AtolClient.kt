package ru.sogaz.site.paymentReceiptService.service

import ru.sogaz.site.paymentReceiptService.model.entity.PaymentDocument
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentItem
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentReceipt

interface AtolClient {
    fun getAtolToken(): String

    fun sendAtolRequest(
        document: PaymentDocument,
        items: List<PaymentItem>,
        payments: List<PaymentReceipt>,
    ): String

    fun getPaymentStatus(document: PaymentDocument): String
}
