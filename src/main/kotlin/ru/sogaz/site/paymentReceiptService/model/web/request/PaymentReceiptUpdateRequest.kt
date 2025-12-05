package ru.sogaz.site.paymentReceiptService.model.web.request

import java.util.UUID

data class PaymentReceiptUpdateRequest(
    val externalId: UUID,
)
