package ru.sogaz.site.paymentReceiptService.model.reference

data class CompanyData(
    val email: String,
    val inn: String,
    val paymentAddress: String,
    val depersonalizedPaymentAddress: String,
)
