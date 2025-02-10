package ru.sogaz.site.paymentReceiptService.repository.reference

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentReceiptService.model.reference.CashRegister
import java.util.UUID

interface CashRegisterRepository : JpaRepository<CashRegister, UUID> {
    fun findBySystemCode(system: String): CashRegister?
}
