package ru.sogaz.site.paymentReceiptService.repository.reference

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentReceiptService.model.reference.VatType
import java.util.UUID

interface VatTypeRepository : JpaRepository<VatType, UUID> {
    fun findByVatTypeCode(code: String): VatType?
}
