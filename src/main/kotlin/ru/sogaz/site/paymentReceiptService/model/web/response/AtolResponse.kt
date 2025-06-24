package ru.sogaz.site.paymentReceiptService.model.web.response

import com.fasterxml.jackson.annotation.JsonProperty

data class AtolResponse(
    @JsonProperty("external_id") val externalId: String,
    @JsonProperty("status") val status: String,
)
