package ru.sogaz.site.paymentReceiptService.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class ValidatorConfig {
    companion object {
        private const val EMAIL_PATTERN = "^(?!\\.)(?!.*\\.\\.)[a-zA-Z0-9._%+-]+(?<!\\.)@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
        private const val PHONE_PATTERN = "^[0-9 ()+\\-]{1,64}$"
        private const val NAME_PATTERN = "^[А-Яа-яЁё \\-]{2,256}$"
        private const val CURRENCY_PATTERN = "^\\d{1,8}(\\.\\d{1,2})?$"
        private const val QUANTITY_PATTERN = "^\\d{1,5}(\\.\\d{1,3})?$"
        const val CURRENCY_MAX: Long = 42_949_672
    }

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
    fun emailRegex(): Regex = Regex(EMAIL_PATTERN)

    @Bean
    fun phoneRegex(): Regex = Regex(PHONE_PATTERN)

    @Bean
    fun nameRegex(): Regex = Regex(NAME_PATTERN)

    @Bean
    fun currencyRegex(): Regex = Regex(CURRENCY_PATTERN)

    @Bean
    fun quantityRegex(): Regex = Regex(QUANTITY_PATTERN)
}
