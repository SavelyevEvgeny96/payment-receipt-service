package ru.sogaz.site.paymentReceiptService.service.atol

import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import ru.sogaz.site.paymentReceiptService.model.reference.Credentials
import java.util.UUID

interface AtolService {
    fun sendReceipt(
        receipt: Receipt,
        credentials: Credentials,
    ): UUID?

    fun getStatus(
        receipt: Receipt,
        credentials: Credentials,
    ): ReceiptState
}
