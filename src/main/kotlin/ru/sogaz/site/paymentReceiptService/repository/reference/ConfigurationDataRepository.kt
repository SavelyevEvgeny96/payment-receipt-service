package ru.sogaz.site.paymentReceiptService.repository.reference

import org.springframework.data.jpa.repository.JpaRepository
import ru.sogaz.site.paymentReceiptService.model.ConfigurationData
import java.util.UUID

interface ConfigurationDataRepository : JpaRepository<ConfigurationData, UUID> {
    abstract fun findByParamName(s: String): ConfigurationData?
}
