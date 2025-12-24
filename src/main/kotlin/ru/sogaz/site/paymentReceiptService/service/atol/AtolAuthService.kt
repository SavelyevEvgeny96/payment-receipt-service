package ru.sogaz.site.paymentReceiptService.service.atol

import ru.sogaz.site.paymentReceiptService.model.credential.Credentials

interface AtolAuthService {
    fun getToken(credentials: Credentials): String
}
