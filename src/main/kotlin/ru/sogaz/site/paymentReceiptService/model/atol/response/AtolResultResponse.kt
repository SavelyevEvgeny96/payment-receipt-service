package ru.sogaz.site.paymentReceiptService.model.atol.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import java.time.Instant
import java.util.UUID

data class AtolResultResponse(
    val uuid: UUID?,
    val status: ReceiptState,
    val payload: AtolResultPayload?,
)

data class AtolResultPayload(
    @field:JsonProperty("receipt_datetime")
    @field:JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss", timezone = "UTC")
    val receiptDatetime: Instant,
    @field:JsonProperty("ofd_receipt_url")
    val ofdReceiptUrl: String,
)
