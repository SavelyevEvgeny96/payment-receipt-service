package ru.sogaz.site.paymentReceiptService.service.credentials

import ru.sogaz.site.paymentReceiptService.model.credential.Credentials
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt

interface CredentialsManager {
    fun findCredentials(receipt: Receipt): Credentials

    fun findCredentials(
        product: String?,
        channel: String?,
    ): Credentials
}
