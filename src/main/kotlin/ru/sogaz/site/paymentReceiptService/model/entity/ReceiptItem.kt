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
    var id: UUID?,
    var name: String?,
    var price: BigDecimal?,
    var quantity: BigDecimal?,
    var sum: BigDecimal?,
    var paymentObject: String?,
    @Enumerated(EnumType.STRING)
    var paymentMethod: PaymentMethod?,
    @Enumerated(EnumType.STRING)
    var vatType: VatType?,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    var receipt: Receipt?,
)
