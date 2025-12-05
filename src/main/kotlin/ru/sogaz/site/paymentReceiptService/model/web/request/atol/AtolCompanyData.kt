package ru.sogaz.site.paymentReceiptService.model.web.request.atol

import com.fasterxml.jackson.annotation.JsonProperty

data class AtolCompanyData(
    val email: String,
    val inn: String,
    @param:JsonProperty("payment_address")
    val paymentAddress: String,
)
