package ru.sogaz.site.paymentReceiptService.model.web.request

data class PaymentReceiptStatusRequest(
    val callbackUrl: String,
    val daemonCode: String,
    val deviceCode: String,
    val error: PaymentCheckError?,
    val externalId: String,
    val groupCode: String,
    val payload: PaymentCheckPayload,
    val status: String,
    val timestamp: String,
    val uuid: String?,
) {
    data class PaymentCheckError(
        val errorId: String,
        val code: Int?,
        val text: String,
        val type: String,
    )

    data class PaymentCheckPayload(
        val ecrRegistrationNumber: String,
        val fiscalDocumentAttribute: Long,
        val fiscalDocumentNumber: Long,
        val fiscalReceiptNumber: Long,
        val fnNumber: String,
        val fnsSite: String,
        val ofdInn: String,
        val receiptDatetime: String,
        val shiftNumber: Int,
        val total: Double,
        val ofdReceiptUrl: String?,
    )
}
