package ru.sogaz.site.paymentReceiptService.model.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.UpdateTimestamp
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptSystem
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptType
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "receipts")
data class Receipt(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID?,
    var paymentId: UUID,
    var orderId: UUID?,
    var link: String?,
    @Enumerated(EnumType.STRING)
    var state: ReceiptState,
    @Enumerated(EnumType.STRING)
    var receiptSystem: ReceiptSystem?,
    @Enumerated(EnumType.STRING)
    var receiptType: ReceiptType,
    var externalId: UUID?,
    var product: String?,
    var channel: String?,
    var total: BigDecimal,
    var clientEmail: String?,
    var clientPhone: String?,
    var depersonalization: Boolean,
    var dateSend: Instant?,
    var sendingTime: Instant?,
    @CreationTimestamp
    var dateCreate: Instant?,
    @UpdateTimestamp
    var dateUpdate: Instant?,
) {
    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY, mappedBy = "receipt")
    @Fetch(FetchMode.SUBSELECT)
    var items: MutableList<ReceiptItem> = mutableListOf()

    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY, mappedBy = "receipt")
    @Fetch(FetchMode.SUBSELECT)
    var payments: MutableList<ReceiptPayment> = mutableListOf()
}
