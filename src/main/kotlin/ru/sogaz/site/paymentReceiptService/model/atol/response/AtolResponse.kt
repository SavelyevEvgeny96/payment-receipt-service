package ru.sogaz.site.paymentReceiptService.model.atol.response

import java.util.UUID

data class AtolResponse(
    val uuid: UUID? = null,
    val status: String?,
    val error: AtolErrorResponse? = null,
    val timestamp: String? = null,
)

data class AtolErrorResponse(
    val code: Int,
    val text: String,
    val type: String,
)
