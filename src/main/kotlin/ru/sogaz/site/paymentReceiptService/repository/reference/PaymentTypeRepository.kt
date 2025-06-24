package ru.sogaz.site.paymentReceiptService.repository.reference

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentReceiptService.model.reference.PaymentType
import java.util.UUID

interface PaymentTypeRepository : JpaRepository<PaymentType, UUID> {
    fun findByTypeIdCode(toInt: Int): PaymentType?
}
