package ru.sogaz.site.paymentReceiptService.model.reference

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payment_types")
data class PaymentType(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val typeId: UUID = UUID.randomUUID(),
    val typeIdCode: Int = 0,
    val typeIdName: String = "",
    val dateCreate: LocalDateTime = LocalDateTime.now(),
    val dateUpdate: LocalDateTime = LocalDateTime.now(),
)
