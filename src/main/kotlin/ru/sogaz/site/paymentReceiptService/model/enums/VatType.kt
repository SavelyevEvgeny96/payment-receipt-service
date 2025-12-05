package ru.sogaz.site.paymentReceiptService.model.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class VatType(
    @JsonValue val desc: String,
) {
    NONE("none"),
    VAT0("vat0"),
    VAT10("vat10"),
    VAT18("vat18"),
    VAT110("vat110"),
    VAT118("vat118"),
    VAT20("vat20"),
    VAT120("vat120"),
}
