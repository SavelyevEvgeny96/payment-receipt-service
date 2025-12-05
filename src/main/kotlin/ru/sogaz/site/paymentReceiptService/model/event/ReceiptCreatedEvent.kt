package ru.sogaz.site.paymentReceiptService.model.event

import java.util.UUID

data class ReceiptCreatedEvent(
    val id: UUID,
)
