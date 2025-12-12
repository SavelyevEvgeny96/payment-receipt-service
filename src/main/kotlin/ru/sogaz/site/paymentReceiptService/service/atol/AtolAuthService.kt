package ru.sogaz.site.paymentReceiptService.service.atol

import ru.sogaz.site.paymentReceiptService.model.atol.request.AtolTokenRequest

interface AtolAuthService {
    fun getToken(atolTokenRequest: AtolTokenRequest): String
}
