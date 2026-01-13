package ru.sogaz.site.paymentReceiptService.model.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class PaymentMethod(
    @JsonValue val desc: String,
) {
    FULL_PREPAYMENT("full_prepayment"),
    PREPAYMENT("prepayment"),
    ADVANCE("advance"),
    FULL_PAYMENT("full_payment"),
    PARTIAL_PAYMENT("partial_payment"),
    CREDIT("credit"),
    CREDIT_PAYMENT("credit_payment"),
}
