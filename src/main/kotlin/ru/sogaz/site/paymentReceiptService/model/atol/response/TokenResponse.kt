package ru.sogaz.site.paymentReceiptService.model.atol.response

import jakarta.validation.constraints.NotBlank

data class TokenResponse(
    @field:NotBlank
    val token: String,
    val error: ErrorInfo? = null,
    val timestamp: String? = null,
)

data class ErrorInfo(
    val error_id: String,
    val code: Int,
    val text: String,
    val type: String,
)
