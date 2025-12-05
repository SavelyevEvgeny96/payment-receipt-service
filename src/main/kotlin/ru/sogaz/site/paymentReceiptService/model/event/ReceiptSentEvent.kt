package ru.sogaz.site.paymentReceiptService.model.event

import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import java.util.UUID

data class ReceiptSentEvent(
    val orderId: UUID,
    val state: ReceiptState,
)
