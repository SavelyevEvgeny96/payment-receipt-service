package ru.sogaz.site.paymentReceiptService.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentReceiptService.loggerFor

@Configuration
@ConfigurationProperties(prefix = "config")
class ConfigurationDataProperties {
    private val logger = loggerFor(javaClass)

    lateinit var atolURL: String
    lateinit var atolLogin: String
    lateinit var atolPass: String
    lateinit var tokenTime: String
    lateinit var callbackURL: String
    lateinit var companyEmail: String
    lateinit var companyInn: String
    lateinit var paymentAddress: String
    lateinit var groupCode: String
    lateinit var periodStatusUpdate: String
}
