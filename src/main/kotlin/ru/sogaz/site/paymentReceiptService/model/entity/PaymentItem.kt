package ru.sogaz.site.paymentReceiptService.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.sogaz.site.paymentReceiptService.model.reference.PaymentMethod
import ru.sogaz.site.paymentReceiptService.model.reference.PaymentObject
import ru.sogaz.site.paymentReceiptService.model.reference.VatType
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payment_items")
data class PaymentItem(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val itemId: UUID? = null,
    var name: String = "",
    var price: Double = 0.0,
    var quantity: Double = 0.0,
    var sum: Double = 0.0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    var paymentMethod: PaymentMethod? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_object_id", nullable = false)
    var paymentObject: PaymentObject? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vat_type_id", nullable = false)
    var vatType: VatType? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id", nullable = false)
    var document: PaymentDocument? = null,
    var dateCreate: LocalDateTime = LocalDateTime.now(),
    var dateUpdate: LocalDateTime = LocalDateTime.now(),
)
