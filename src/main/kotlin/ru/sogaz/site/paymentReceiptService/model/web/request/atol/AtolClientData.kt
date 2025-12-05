package ru.sogaz.site.paymentReceiptService.model.web.request.atol

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AtolClientData(
    val email: String,
    val phone: String?,
)
