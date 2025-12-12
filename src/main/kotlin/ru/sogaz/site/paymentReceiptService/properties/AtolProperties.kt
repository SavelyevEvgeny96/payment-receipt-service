package ru.sogaz.site.paymentReceiptService.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.paymentReceiptService.model.reference.CompanyData
import ru.sogaz.site.paymentReceiptService.model.reference.Credentials
import ru.sogaz.site.paymentReceiptService.model.reference.ServiceData

@ConfigurationProperties(prefix = "config.atol")
class AtolProperties {
    lateinit var credentials: Credentials
    lateinit var company: CompanyData
    lateinit var callback: ServiceData
}
