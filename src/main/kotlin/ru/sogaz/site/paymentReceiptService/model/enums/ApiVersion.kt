package ru.sogaz.site.paymentReceiptService.model.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class ApiVersion(
    @JsonValue val desc: String,
) {
    V4("v4"),
}
