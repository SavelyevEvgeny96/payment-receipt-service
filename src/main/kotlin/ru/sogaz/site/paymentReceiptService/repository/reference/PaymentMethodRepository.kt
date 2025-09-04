package ru.sogaz.site.paymentReceiptService.repository.reference

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentReceiptService.model.reference.PaymentMethod
import java.util.UUID

interface PaymentMethodRepository : JpaRepository<PaymentMethod, UUID> {
    fun findByPaymentMethodCode(code: String): PaymentMethod?
}
