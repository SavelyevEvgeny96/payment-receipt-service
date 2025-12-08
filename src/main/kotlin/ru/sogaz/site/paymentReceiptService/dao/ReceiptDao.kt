package ru.sogaz.site.paymentReceiptService.dao

import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import java.time.LocalDateTime
import java.util.UUID

interface ReceiptDao {
    @Transactional(rollbackFor = [Exception::class])
    fun save(receipt: Receipt): Receipt

    fun findById(id: UUID): Receipt?

    fun findByOrderId(orderId: UUID): Receipt?

    fun findByExternalId(externalId: UUID): Receipt?

    fun findByStatusAndDateSendBetween(
        state: ReceiptState,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<Receipt>
}
