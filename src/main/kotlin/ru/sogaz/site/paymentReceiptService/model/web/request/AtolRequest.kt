package ru.sogaz.site.paymentReceiptService.model.web.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class AtolRequest(
    @JsonProperty("external_id") val externalId: String,
    @JsonProperty("service") val service: AtolServiceData,
    @JsonProperty("receipt") val receipt: AtolReceiptData,
    @JsonProperty("timestamp") val timestamp: String,
) {
    data class AtolServiceData(
        @JsonProperty("callback_url") val callbackUrl: String,
    )

    data class AtolReceiptData(
        @JsonProperty("client") val client: AtolClientData,
        @JsonProperty("company") val company: AtolCompanyData,
        @JsonProperty("items") val items: List<AtolItemData>,
        @JsonProperty("payments") val payments: List<AtolPaymentData>,
        @JsonProperty("total") val total: Double,
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class AtolClientData(
        @JsonProperty("email") val email: String,
        @JsonProperty("phone") val phone: String?,
    )

    data class AtolCompanyData(
        @JsonProperty("email") val email: String,
        @JsonProperty("inn") val inn: String,
        @JsonProperty("payment_address") val paymentAddress: String,
    )

    data class AtolItemData(
        @JsonProperty("name") val name: String,
        @JsonProperty("price") val price: Double,
        @JsonProperty("quantity") val quantity: Double,
        @JsonProperty("sum") val sum: Double,
        @JsonProperty("payment_method") val paymentMethod: String,
        @JsonProperty("payment_object") val paymentObject: String,
        @JsonProperty("vat") val vat: AtolVatData,
    )

    data class AtolVatData(
        @JsonProperty("type") val type: String,
    )

    data class AtolPaymentData(
        @JsonProperty("sum") val sum: Double,
        @JsonProperty("type") val type: Int,
    )
}
