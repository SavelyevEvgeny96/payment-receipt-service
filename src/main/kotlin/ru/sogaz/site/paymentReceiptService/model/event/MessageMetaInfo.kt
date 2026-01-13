package ru.sogaz.site.paymentReceiptService.model.event

import java.time.Instant

data class MessageMetaInfo(
    val eventTimeIso: Instant?,
    val author: String,
    val routingKey: String,
)
