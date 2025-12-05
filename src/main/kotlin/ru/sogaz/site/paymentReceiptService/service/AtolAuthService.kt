package ru.sogaz.site.paymentReceiptService.service

import ru.sogaz.site.paymentReceiptService.model.reference.AtolCredentials

interface AtolAuthService {
    fun getToken(atolCredentials: AtolCredentials): String
}
