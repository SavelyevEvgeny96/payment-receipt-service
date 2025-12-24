package ru.sogaz.site.paymentReceiptService.model.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class ReceiptType(
    @JsonValue val desc: String,
) {
    SELL("sell"),
    REFUND("sell_refund"),
}
