package ru.sogaz.site.paymentReceiptService.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentDocument
import ru.sogaz.site.paymentReceiptService.model.reference.CheckStatus
import java.time.LocalDateTime
import java.util.UUID

interface PaymentDocumentRepository : JpaRepository<PaymentDocument, UUID> {
    fun findByExternalId(externalId: String): PaymentDocument?

    @Query(
        "SELECT d FROM PaymentDocument d " +
            "WHERE d.status IN :states " +
            "AND d.dateSend BETWEEN :startTime AND :endTime",
    )
    fun findByStatusAndDateSendBetween(
        states: List<CheckStatus>,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<PaymentDocument>
}
