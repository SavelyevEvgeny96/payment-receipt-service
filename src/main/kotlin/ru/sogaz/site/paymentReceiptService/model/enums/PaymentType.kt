package ru.sogaz.site.paymentReceiptService.model.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class PaymentType(
    @JsonValue val desc: Int,
) {
    CASH(0),
    NON_CASH(1),
    ADVANCE_PAYMENT(2),
    POST_PAYMENT(3),
    OTHER_FORM_OF_PAYMENT(4),
}
