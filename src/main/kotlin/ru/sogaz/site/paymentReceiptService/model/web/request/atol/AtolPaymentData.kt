package ru.sogaz.site.paymentReceiptService.model.web.request.atol

import java.math.BigDecimal

data class AtolPaymentData(
    val sum: BigDecimal,
    val type: Int,
)
