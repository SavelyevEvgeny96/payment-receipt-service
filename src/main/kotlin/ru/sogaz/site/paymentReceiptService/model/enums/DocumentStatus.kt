package ru.sogaz.site.paymentReceiptService.model.enums

enum class DocumentStatus(
    val value: String,
) {
    WAIT("wait"),
    DONE("done"),
    FAIL("fail"),
}
