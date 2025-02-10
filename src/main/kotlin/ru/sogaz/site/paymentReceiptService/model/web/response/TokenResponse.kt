package ru.sogaz.site.paymentReceiptService.model.web.response

data class TokenResponse(
    val error: String?,
    val token: String?,
    val timestamp: String?,
)
