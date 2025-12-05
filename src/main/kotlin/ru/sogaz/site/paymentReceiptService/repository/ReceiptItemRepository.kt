package ru.sogaz.site.paymentReceiptService.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentReceiptService.model.entity.ReceiptItem
import java.util.UUID

interface ReceiptItemRepository : JpaRepository<ReceiptItem, UUID>
