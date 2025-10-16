package ru.sogaz.site.paymentReceiptService.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.paymentReceiptService.mapper.PaymentDocumentMapper
import ru.sogaz.site.paymentReceiptService.mapper.PaymentItemMapper
import ru.sogaz.site.paymentReceiptService.mapper.PaymentReceiptMapper
import ru.sogaz.site.paymentReceiptService.properties.ConfigurationDataProperties
import ru.sogaz.site.paymentReceiptService.repository.PaymentDocumentRepository
import ru.sogaz.site.paymentReceiptService.repository.PaymentItemRepository
import ru.sogaz.site.paymentReceiptService.repository.PaymentReceiptRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.ApiVersionRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.CashRegisterRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.CheckStatusRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.PaymentMethodRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.PaymentObjectRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.PaymentTypeRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.VatTypeRepository
import ru.sogaz.site.paymentReceiptService.service.AtolClient
import ru.sogaz.site.paymentReceiptService.service.PaymentReceiptService
import ru.sogaz.site.paymentReceiptService.service.impl.AtolClientImpl
import ru.sogaz.site.paymentReceiptService.service.impl.PaymentReceiptServiceImpl
import ru.sogaz.site.paymentReceiptService.validation.PaymentReceiptCreateRequestValidation
import ru.sogaz.site.paymentReceiptService.validation.paramValidation.CurrencyValidator
import ru.sogaz.site.paymentReceiptService.validation.paramValidation.EmailValidator
import ru.sogaz.site.paymentReceiptService.validation.paramValidation.NameValidator
import ru.sogaz.site.paymentReceiptService.validation.paramValidation.PhoneValidator
import ru.sogaz.site.paymentReceiptService.validation.paramValidation.QuantityValidator

@Configuration
class ApiConfig(
    private val paymentDocumentRepository: PaymentDocumentRepository,
    private val paymentItemRepository: PaymentItemRepository,
    private val paymentReceiptRepository: PaymentReceiptRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentObjectRepository: PaymentObjectRepository,
    private val vatTypeRepository: VatTypeRepository,
    private val paymentTypeRepository: PaymentTypeRepository,
    private val apiVersionRepository: ApiVersionRepository,
    private val cashRegisterRepository: CashRegisterRepository,
    private val checkStatusRepository: CheckStatusRepository,
    private val paymentDocumentMapper: PaymentDocumentMapper,
    private val paymentItemMapper: PaymentItemMapper,
    private val paymentReceiptMapper: PaymentReceiptMapper,
    private val cacheManager: CacheManager,
    private val configurationDataProperties: ConfigurationDataProperties,
) {
    @Bean
    @Primary
    fun objectsMapper(): ObjectMapper =
        ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()
            .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)

    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()

        restTemplate.messageConverters.removeIf { it is MappingJackson2XmlHttpMessageConverter }

        restTemplate.messageConverters.add(MappingJackson2HttpMessageConverter())

        return restTemplate
    }

    @Bean
    fun atolClient(): AtolClient =
        AtolClientImpl(
            restTemplate(),
            cacheManager,
            configurationDataProperties,
            objectsMapper(),
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

    @Bean
    fun phoneValidator(): PhoneValidator {
        val phoneRegex = Regex("^[0-9 ()+\\-]{1,64}\$")
        return PhoneValidator(phoneRegex)
    }

    @Bean
    fun emailValidator(): EmailValidator {
        val emailRegex = Regex("^(?!.*\\.\\.)(?!\\.)(?!.*\\.\$)[A-Za-z0-9!#\$%&'*+/=?^_`{|}~.-]+@[A-Za-z0-9.-]+\$")
        return EmailValidator(emailRegex)
    }

    @Bean
    fun nameValidator(): NameValidator {
        val nameRegex = Regex("^[А-Яа-яЁё \\-]{2,256}\$")
        return NameValidator(nameRegex)
    }

    @Bean
    fun currencyValidator(): CurrencyValidator {
        val currencyRegex = Regex("^\\d{1,8}(\\.\\d{1,2})?$")
        val max = 42_949_672.95
        return CurrencyValidator(currencyRegex, max)
    }

    @Bean
    fun quantityValidator(): QuantityValidator {
        val nameRegex = Regex("^\\d{1,5}(\\.\\d{1,3})?$")
        return QuantityValidator(nameRegex)
    }

    @Bean
    fun paymentReceiptCreateRequestValidation(): PaymentReceiptCreateRequestValidation =
        PaymentReceiptCreateRequestValidation(
            phoneValidator(),
            emailValidator(),
            nameValidator(),
            currencyValidator(),
            quantityValidator(),
            paymentMethodRepository,
            paymentObjectRepository,
            vatTypeRepository,
            paymentTypeRepository,
            apiVersionRepository,
            cashRegisterRepository,
        )
}
