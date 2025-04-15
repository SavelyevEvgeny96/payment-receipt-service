package ru.sogaz.site.paymentReceiptService.model.reference

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "check_statuses")
data class CheckStatus(
    @Id
    val id: UUID,
    val stateId: String,
    val stateName: String,
) {
    constructor() : this(
        id = UUID.randomUUID(),
        stateId = "",
        stateName = "",
    )
}
