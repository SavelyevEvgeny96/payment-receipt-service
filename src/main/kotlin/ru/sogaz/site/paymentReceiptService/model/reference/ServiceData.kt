package ru.sogaz.site.paymentReceiptService.model.reference

import com.fasterxml.jackson.annotation.JsonProperty

data class ServiceData(
    @param:JsonProperty("callback_url")
    val url: String,
)
