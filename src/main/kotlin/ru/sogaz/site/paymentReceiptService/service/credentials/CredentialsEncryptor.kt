package ru.sogaz.site.paymentReceiptService.service.credentials

import ru.sogaz.site.paymentReceiptService.model.entity.CheckoutMapping
import ru.sogaz.site.paymentReceiptService.model.reference.Credentials

interface CredentialsEncryptor {
    fun encryptCredentials(checkoutMapping: CheckoutMapping): Credentials
}
