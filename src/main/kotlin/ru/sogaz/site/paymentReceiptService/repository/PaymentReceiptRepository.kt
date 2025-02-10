package ru.sogaz.site.paymentReceiptService.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentReceipt
import java.util.UUID

interface PaymentReceiptRepository : JpaRepository<PaymentReceipt, UUID>
