package ru.sogaz.site.paymentReceiptService.repository.reference

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentReceiptService.model.reference.CheckStatus
import java.util.UUID

interface CheckStatusRepository : JpaRepository<CheckStatus, UUID> {
    fun findByStateId(s: String): CheckStatus?
}
