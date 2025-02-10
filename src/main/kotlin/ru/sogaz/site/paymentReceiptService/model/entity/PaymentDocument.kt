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
data class PaymentDocument(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val docId: UUID = UUID.randomUUID(),
    val clientUserId: String?,
    val clientEmail: String,
    val clientPhone: String?,
    val clientName: String?,
    val total: Double,
    var externalId: String?,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_id")
    val cashRegister: CashRegister,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id")
    val apiVersion: ApiVersion,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    var status: CheckStatus,
    var dateSend: LocalDateTime?,
    val dateCreate: LocalDateTime = LocalDateTime.now(),
    var dateUpdate: LocalDateTime = LocalDateTime.now(),
)
