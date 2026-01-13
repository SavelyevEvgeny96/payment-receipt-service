package ru.sogaz.site.paymentReceiptService.model.web.request

import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import java.util.UUID

data class PaymentReceiptStatusRequest(
    @JsonProperty("callback_url")
    val callbackUrl: String,
    @JsonProperty("daemon_code")
    val daemonCode: String,
    @JsonProperty("device_code")
    val deviceCode: String,
    @JsonProperty("error")
    val error: PaymentCheckError?,
    @JsonProperty("external_id")
    val externalId: UUID,
    @JsonProperty("group_code")
    val groupCode: String,
    @JsonProperty("payload")
    val payload: PaymentCheckPayload,
    @JsonProperty("status")
    val status: ReceiptState,
    @JsonProperty("timestamp")
    val timestamp: String,
    @JsonProperty("uuid")
    val uuid: String?,
) {
    data class PaymentCheckError(
        @JsonProperty("error_id")
        val errorId: String,
        @JsonProperty("code")
        val code: Int?,
        @JsonProperty("text")
        val text: String,
        @JsonProperty("type")
        val type: String,
    )

    data class PaymentCheckPayload(
        @JsonProperty("ecr_registration_number")
        val ecrRegistrationNumber: String,
        @JsonProperty("fiscal_document_attribute")
        val fiscalDocumentAttribute: Long,
        @JsonProperty("fiscal_document_number")
        val fiscalDocumentNumber: Long,
        @JsonProperty("fiscal_receipt_number")
        val fiscalReceiptNumber: Long,
        @JsonProperty("fn_number")
        val fnNumber: String,
        @JsonProperty("fns_site")
        val fnsSite: String,
        @JsonProperty("ofd_inn")
        val ofdInn: String,
        @JsonProperty("receipt_datetime")
        val receiptDatetime: String,
        @JsonProperty("shift_number")
        val shiftNumber: Int,
        @JsonProperty("total")
        val total: Double,
        @JsonProperty("ofd_receipt_url")
        val ofdReceiptUrl: String?,
    )
}
