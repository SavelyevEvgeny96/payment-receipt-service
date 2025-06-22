package ru.sogaz.site.paymentReceiptService.model.web.request

data class PaymentReceiptCreateRequest(
    val client: ClientInfo,
    val userId: String?,
    val items: List<PaymentItemRequest>,
    val payments: List<PaymentPaymentRequest>,
    val total: Double,
    val system: String,
    val version: String,
) {
    data class ClientInfo(
        val email: String,
        val phone: String?,
        val name: String?,
    )

    data class PaymentItemRequest(
        val name: String,
        val price: Double,
        val quantity: Double,
        val sum: Double,
        val paymentMethod: String,
        val paymentObject: String,
        val vat: VatRequest,
    )

    data class VatRequest(
        val type: String,
    )

    data class PaymentPaymentRequest(
        val type: String,
        val sum: Double,
    )
}
