package ru.sogaz.site.paymentReceiptService.model.atol.request

data class AtolTokenRequest(
    val login: String,
    val pass: String,
    val groupCode: String,
)
