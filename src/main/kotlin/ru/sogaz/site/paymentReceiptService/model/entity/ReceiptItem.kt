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
import ru.sogaz.site.paymentReceiptService.model.enums.PaymentMethod
import ru.sogaz.site.paymentReceiptService.model.enums.VatType
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "receipt_items")
class ReceiptItem(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var name: String? = null,
    var price: BigDecimal? = null,
    var quantity: BigDecimal? = null,
    var sum: BigDecimal? = null,
    var paymentObject: String? = null,
    @Enumerated(EnumType.STRING)
    var paymentMethod: PaymentMethod? = null,
    @Enumerated(EnumType.STRING)
    var vatType: VatType? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    var receipt: Receipt? = null,
)
