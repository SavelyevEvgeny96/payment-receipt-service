package ru.sogaz.site.paymentReceiptService.model.web.request.atol

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

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
