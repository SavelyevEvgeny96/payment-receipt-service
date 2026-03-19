package ru.sogaz.site.paymentReceiptService.dao

import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import java.time.Instant
import java.util.UUID

interface ReceiptDao {
    fun save(receipt: Receipt): Receipt

    fun findById(id: UUID): Receipt?

    fun findByOrderId(orderId: UUID): Receipt?

    fun findByExternalId(externalId: UUID): Receipt?

    fun findByStatusAndDateSendBetween(
        state: ReceiptState,
        startTime: Instant,
        endTime: Instant,
    ): List<Receipt>
}
