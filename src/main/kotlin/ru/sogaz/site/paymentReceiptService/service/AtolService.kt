package ru.sogaz.site.paymentReceiptService.service

import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import java.util.UUID

interface AtolService {
    fun sendReceipt(receipt: Receipt): UUID?

    fun getStatus(externalId: UUID): ReceiptState
}
