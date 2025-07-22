package ru.sogaz.site.paymentReceiptService.model.web.response

data class TokenResponse(
    val token: String?,
    val error: ErrorInfo? = null,
    val timestamp: String? = null,
)

data class ErrorInfo(
    val error_id: String,
    val code: Int,
    val text: String,
    val type: String,
)
