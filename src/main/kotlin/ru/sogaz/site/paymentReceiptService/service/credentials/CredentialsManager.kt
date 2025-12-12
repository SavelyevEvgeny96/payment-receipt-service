package ru.sogaz.site.paymentReceiptService.service.credentials

import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.reference.Credentials

interface CredentialsManager {
    fun findCredentials(receipt: Receipt): Credentials

    fun findCredentials(
        product: String?,
        channel: String?,
    ): Credentials
}
