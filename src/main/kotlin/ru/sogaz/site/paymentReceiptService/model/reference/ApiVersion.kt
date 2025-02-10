package ru.sogaz.site.paymentReceiptService.model.reference

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "api_versions")
data class ApiVersion(
    @Id
    val versionId: UUID,
    val versionCode: String,
    val versionName: String,
    val dateCreate: LocalDateTime = LocalDateTime.now(),
    val dateUpdate: LocalDateTime = LocalDateTime.now(),
)
