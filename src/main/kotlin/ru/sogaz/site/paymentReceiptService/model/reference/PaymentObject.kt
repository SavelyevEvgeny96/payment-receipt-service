package ru.sogaz.site.paymentReceiptService.model.reference

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payment_objects")
data class PaymentObject(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val paymentObjectId: UUID = UUID.randomUUID(),
    val paymentObjectIdCode: String,
    val paymentObjectIdName: String,
    val dateCreate: LocalDateTime = LocalDateTime.now(),
    val dateUpdate: LocalDateTime = LocalDateTime.now(),
)
