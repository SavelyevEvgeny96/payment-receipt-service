package ru.sogaz.site.paymentReceiptService.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

interface ReceiptRepository : JpaRepository<Receipt, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(docId: UUID): Optional<Receipt>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByExternalId(externalId: UUID): Receipt?

    fun findByStateAndDateSendBetween(
        state: ReceiptState,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<Receipt>
}
