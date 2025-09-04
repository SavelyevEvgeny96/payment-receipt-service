package ru.sogaz.site.paymentReceiptService.repository.reference

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentReceiptService.model.reference.ApiVersion
import java.util.UUID

interface ApiVersionRepository : JpaRepository<ApiVersion, UUID> {
    fun findByVersionCode(version: String): ApiVersion?
}
