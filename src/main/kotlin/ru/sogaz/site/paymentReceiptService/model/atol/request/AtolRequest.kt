package ru.sogaz.site.paymentReceiptService.model.atol.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentReceiptService.model.reference.ServiceData
import java.math.BigDecimal

data class AtolRequest(
    @param:JsonProperty("external_id")
    val externalId: String,
    val service: ServiceData,
    val receipt: AtolReceiptData,
    var timestamp: String?,
)

data class AtolServiceData(
    @param:JsonProperty("callback_url")
    val url: String,
)

data class AtolReceiptData(
    val client: AtolClientData,
    val company: AtolCompanyData,
    val items: List<AtolItemData>,
    val payments: List<AtolPaymentData>,
    val total: BigDecimal,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AtolClientData(
    val email: String,
    val phone: String?,
)

data class AtolCompanyData(
    val email: String,
    val inn: String,
    @param:JsonProperty("payment_address")
    val paymentAddress: String,
)

data class AtolItemData(
    val name: String,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val sum: BigDecimal,
    @param:JsonProperty("payment_method")
    val paymentMethod: String,
    @param:JsonProperty("payment_object")
    val paymentObject: String,
    val vat: AtolVatData,
)

data class AtolPaymentData(
    val sum: BigDecimal,
    val type: Int,
)

data class AtolVatData(
    @JsonProperty("type") val type: String,
)
