package ru.sogaz.site.paymentReceiptService.config

import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.paymentReceiptService.mapper.PaymentDocumentMapper
import ru.sogaz.site.paymentReceiptService.mapper.PaymentItemMapper
import ru.sogaz.site.paymentReceiptService.mapper.PaymentReceiptMapper
import ru.sogaz.site.paymentReceiptService.properties.ConfigurationDataProperties
import ru.sogaz.site.paymentReceiptService.repository.PaymentDocumentRepository
import ru.sogaz.site.paymentReceiptService.repository.PaymentItemRepository
import ru.sogaz.site.paymentReceiptService.repository.PaymentReceiptRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.CheckStatusRepository
import ru.sogaz.site.paymentReceiptService.service.AtolClient
import ru.sogaz.site.paymentReceiptService.service.PaymentReceiptService
import ru.sogaz.site.paymentReceiptService.service.impl.AtolClientImpl
import ru.sogaz.site.paymentReceiptService.service.impl.PaymentReceiptServiceImpl

@Configuration
class ApiConfig(
    private val paymentDocumentRepository: PaymentDocumentRepository,
    private val paymentItemRepository: PaymentItemRepository,
    private val paymentReceiptRepository: PaymentReceiptRepository,
    private val checkStatusRepository: CheckStatusRepository,
    private val paymentDocumentMapper: PaymentDocumentMapper,
    private val paymentItemMapper: PaymentItemMapper,
    private val paymentReceiptMapper: PaymentReceiptMapper,
    private val cacheManager: CacheManager,
    private val configurationDataProperties: ConfigurationDataProperties,
) {
    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()

    @Bean
    fun atolClient(): AtolClient =
        AtolClientImpl(
            restTemplate(),
            cacheManager,
            configurationDataProperties,
        )

    @Bean
    fun paymentReceiptService(): PaymentReceiptService =
        PaymentReceiptServiceImpl(
            paymentDocumentRepository,
            paymentItemRepository,
            paymentReceiptRepository,
            checkStatusRepository,
            atolClient(),
            paymentDocumentMapper,
            paymentItemMapper,
            paymentReceiptMapper,
        )
}
