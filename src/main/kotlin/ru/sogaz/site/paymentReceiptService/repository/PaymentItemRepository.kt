package ru.sogaz.site.paymentReceiptService.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentItem
import java.util.UUID

interface PaymentItemRepository : JpaRepository<PaymentItem, UUID>
