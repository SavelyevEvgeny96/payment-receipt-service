package ru.sogaz.site.paymentReceiptService.model.reference

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payment_methods")
data class PaymentMethod(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val paymentMethodId: UUID? = null,
    val paymentMethodCode: String = "",
    val paymentMethodName: String = "",
    val dateCreate: LocalDateTime = LocalDateTime.now(),
    val dateUpdate: LocalDateTime = LocalDateTime.now(),
)
