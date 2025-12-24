package ru.sogaz.site.paymentReceiptService.service.credentials

import ru.sogaz.site.paymentReceiptService.model.credential.Credentials
import ru.sogaz.site.paymentReceiptService.model.entity.CheckoutMapping

interface CredentialsEncryptor {
    fun encryptCredentials(checkoutMapping: CheckoutMapping): Credentials
}
