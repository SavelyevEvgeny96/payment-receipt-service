package ru.sogaz.site.paymentReceiptService

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
@EnableFeignClients(basePackages = ["ru.sogaz.site.paymentReceiptService.clients", "ru.sogaz.site.paymentReceiptService.taxcom.client"])
@ConfigurationPropertiesScan("ru.sogaz.site.paymentReceiptService.properties")
open class KotlinTemplateApplication

fun main(args: Array<String>) {
    runApplication<KotlinTemplateApplication>(*args)
}

inline fun <reified T> T?.orThrow(block: () -> Exception): T = this ?: throw block()
