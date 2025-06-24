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
    @GeneratedValue(strategy = GenerationType.AUTO)
    val itemId: UUID = UUID.randomUUID(),
    val name: String,
    val price: Double,
    val quantity: Double,
    val sum: Double,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    val paymentMethod: PaymentMethod,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_object_id")
    val paymentObject: PaymentObject,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vat_type_id")
    val vatType: VatType,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doc_id")
    val document: PaymentDocument,
    val dateCreate: LocalDateTime = LocalDateTime.now(),
    val dateUpdate: LocalDateTime = LocalDateTime.now(),
)
