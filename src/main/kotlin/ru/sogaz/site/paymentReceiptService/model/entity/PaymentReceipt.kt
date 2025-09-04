package ru.sogaz.site.paymentReceiptService.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.sogaz.site.paymentReceiptService.model.reference.PaymentType
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payment_receipts")
data class PaymentReceipt(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var paymentId: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    var paymentType: PaymentType? = null,
    var sum: Double = 0.0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id")
    var document: PaymentDocument? = null,
    var dateCreate: LocalDateTime = LocalDateTime.now(),
    var dateUpdate: LocalDateTime = LocalDateTime.now(),
)
