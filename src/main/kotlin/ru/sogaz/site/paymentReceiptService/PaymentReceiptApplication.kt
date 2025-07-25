package ru.sogaz.site.paymentReceiptService

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import ru.sogaz.site.paymentReceiptService.properties.AppInfoProperties
import ru.sogaz.site.paymentReceiptService.properties.ConfigurationDataProperties

@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan("ru.sogaz.site.payment-receipt-service.properties")
@EnableConfigurationProperties(AppInfoProperties::class, ConfigurationDataProperties::class)
open class KotlinTemplateApplication

fun main(args: Array<String>) {
    runApplication<KotlinTemplateApplication>(*args)
}

fun <T> loggerFor(clazz: Class<T>) =
    ru.sogaz.core.logger.LoggerFactory
        .getApiLogger(clazz)
