package ru.sogaz.site.paymentReceiptService.model.event

import com.fasterxml.jackson.annotation.JsonGetter
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptType
import java.time.Instant
import java.util.UUID

data class ReceiptSentEvent(
    val orderId: UUID,
    val paymentId: UUID,
    val receiptId: UUID,
    val amount: String,
    val state: ReceiptState,
    val typeOperation: ReceiptType,
    val sendingTime: Instant,
    val link: String,
) {
    @JsonGetter("typeOperation")
    fun getTypeOperation(): String = typeOperation.desc.uppercase()
}

