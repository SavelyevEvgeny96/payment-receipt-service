package ru.sogaz.site.paymentReceiptService.repository.reference

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentReceiptService.model.reference.PaymentObject
import java.util.UUID

interface PaymentObjectRepository : JpaRepository<PaymentObject, UUID> {
    fun findByPaymentObjectIdCode(code: String): PaymentObject?
}
