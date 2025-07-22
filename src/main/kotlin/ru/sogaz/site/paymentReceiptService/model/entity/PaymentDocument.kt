package ru.sogaz.site.paymentReceiptService.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.sogaz.site.paymentReceiptService.model.reference.ApiVersion
import ru.sogaz.site.paymentReceiptService.model.reference.CashRegister
import ru.sogaz.site.paymentReceiptService.model.reference.CheckStatus
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payment_documents")
class PaymentDocument(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var docId: UUID? = null,
    var clientUserId: String? = null,
    var clientEmail: String = "",
    var clientPhone: String? = null,
    var clientName: String? = null,
    var total: Double = 0.0,
    var externalId: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_id")
    var cashRegister: CashRegister? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id")
    var apiVersion: ApiVersion? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    var status: CheckStatus? = null,
    var dateSend: LocalDateTime? = null,
    var dateCreate: LocalDateTime = LocalDateTime.now(),
    var dateUpdate: LocalDateTime = LocalDateTime.now(),
)
