package ru.sogaz.site.paymentReceiptService.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentReceiptService.model.entity.CheckoutMapping
import java.util.UUID

interface CheckoutMappingRepository : JpaRepository<CheckoutMapping, UUID>
