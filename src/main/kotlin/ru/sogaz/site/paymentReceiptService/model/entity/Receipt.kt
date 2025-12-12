package ru.sogaz.site.paymentReceiptService.model.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
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
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "receipts")
data class Receipt(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var orderId: UUID? = null,
    @Column(name = "state", columnDefinition = "VARCHAR(50)")
    @Enumerated(EnumType.STRING)
    var state: ReceiptState = ReceiptState.NEW,
    @Enumerated(EnumType.STRING)
    var receiptSystem: ReceiptSystem? = null,
    var externalId: UUID? = null,
    var product: String? = null,
    var channel: String? = null,
    var total: BigDecimal? = null,
    var clientEmail: String? = null,
    var clientPhone: String? = null,
    var depersonalization: Boolean = false,
    var dateSend: LocalDateTime? = null,
    @CreationTimestamp
    var dateCreate: LocalDateTime? = null,
    @UpdateTimestamp
    var dateUpdate: LocalDateTime? = null,
) {
    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY, mappedBy = "receipt")
    @Fetch(FetchMode.SUBSELECT)
    var items: MutableList<ReceiptItem> = mutableListOf()

    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY, mappedBy = "receipt")
    @Fetch(FetchMode.SUBSELECT)
    var payments: MutableList<ReceiptPayment> = mutableListOf()
}
