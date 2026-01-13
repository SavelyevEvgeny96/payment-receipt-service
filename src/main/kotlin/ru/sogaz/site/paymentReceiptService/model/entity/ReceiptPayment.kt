package ru.sogaz.site.paymentReceiptService.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.sogaz.site.paymentReceiptService.model.enums.PaymentType
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "receipt_payments")
class ReceiptPayment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var sum: BigDecimal? = null,
    @Enumerated(EnumType.STRING)
    var paymentType: PaymentType? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    var receipt: Receipt? = null,
)
