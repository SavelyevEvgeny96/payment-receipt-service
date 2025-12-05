package ru.sogaz.site.paymentReceiptService.model.web.request.atol

import com.fasterxml.jackson.annotation.JsonProperty

data class AtolServiceData(
    @JsonProperty("callback_url") val url: String,
)
