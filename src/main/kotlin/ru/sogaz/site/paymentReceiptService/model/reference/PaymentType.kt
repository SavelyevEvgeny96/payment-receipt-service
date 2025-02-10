package ru.sogaz.site.paymentReceiptService.model.reference

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payment_types")
data class PaymentType(
    @Id
    val typeId: UUID,
    val typeIdCode: Int,
    val typeIdName: String,
    val dateCreate: LocalDateTime = LocalDateTime.now(),
    val dateUpdate: LocalDateTime = LocalDateTime.now(),
)
