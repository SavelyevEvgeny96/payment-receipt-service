package ru.sogaz.site.paymentReceiptService.model.atol.response

import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState

data class AtolStatusResponse(
    val status: ReceiptState,
)
