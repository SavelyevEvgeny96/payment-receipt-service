package ru.sogaz.site.paymentReceiptService.model.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class ReceiptSystem(
    @JsonValue val desc: String,
) {
    ATOL("atol"),
}
