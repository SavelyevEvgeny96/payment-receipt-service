package ru.sogaz.site.paymentReceiptService.model.web.request.atol

import ru.sogaz.site.paymentReceiptService.model.web.request.atol.AtolCompanyData
import java.math.BigDecimal

data class AtolReceiptData(
    val client: AtolClientData,
    val company: AtolCompanyData,
    val items: List<AtolItemData>,
    val payments: List<AtolPaymentData>,
    val total: BigDecimal,
)
