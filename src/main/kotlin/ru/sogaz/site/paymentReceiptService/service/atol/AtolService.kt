package ru.sogaz.site.paymentReceiptService.service.atol

import ru.sogaz.site.paymentReceiptService.model.atol.response.AtolResultResponse
import ru.sogaz.site.paymentReceiptService.model.credential.Credentials
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import java.util.UUID

interface AtolService {
    fun sendReceipt(
        receipt: Receipt,
        credentials: Credentials,
    ): UUID?

    fun getResult(
        receipt: Receipt,
        credentials: Credentials,
    ): AtolResultResponse
}
