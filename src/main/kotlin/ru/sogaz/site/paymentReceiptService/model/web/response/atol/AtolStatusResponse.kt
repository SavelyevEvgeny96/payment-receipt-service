package ru.sogaz.site.paymentReceiptService.model.web.response.atol

import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState

data class AtolStatusResponse(
    val status: ReceiptState,
)
