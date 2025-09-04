package ru.sogaz.site.paymentReceiptService.model.web.response

import com.fasterxml.jackson.annotation.JsonProperty

data class AtolResponse(
    @JsonProperty("uuid")
    val uuid: String,
    @JsonProperty("status")
    val status: String,
    @JsonProperty("error")
    val error: AtolErrorResponse? = null,
    @JsonProperty("timestamp")
    val timestamp: String,
)

data class AtolErrorResponse(
    @JsonProperty("code")
    val code: Int,
    @JsonProperty("text")
    val text: String,
    @JsonProperty("type")
    val type: String,
)
