package ru.sogaz.site.paymentReceiptService.model.web.request.atol

import com.fasterxml.jackson.annotation.JsonProperty

data class AtolVatData(
    @JsonProperty("type") val type: String,
)
