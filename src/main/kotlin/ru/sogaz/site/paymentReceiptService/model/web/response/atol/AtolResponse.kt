package ru.sogaz.site.paymentReceiptService.model.web.response.atol

import java.util.UUID

data class AtolResponse(
    val uuid: UUID?,
    val status: String?,
    val error: AtolErrorResponse? = null,
    val timestamp: String?,
)

data class AtolErrorResponse(
    val code: Int,
    val text: String,
    val type: String,
)
