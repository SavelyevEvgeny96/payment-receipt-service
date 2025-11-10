package ru.sogaz.site.paymentReceiptService.model.reference

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "vat_types")
data class VatType(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val vatTypeId: UUID? = null,
    val vatTypeCode: String = "",
    val vatTypeName: String = "",
    val dateCreate: LocalDateTime = LocalDateTime.now(),
    val dateUpdate: LocalDateTime = LocalDateTime.now(),
)
