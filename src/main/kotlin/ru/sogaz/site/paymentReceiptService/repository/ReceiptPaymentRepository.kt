package ru.sogaz.site.paymentReceiptService.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentReceiptService.model.entity.ReceiptPayment
import java.util.UUID

interface ReceiptPaymentRepository : JpaRepository<ReceiptPayment, UUID>
