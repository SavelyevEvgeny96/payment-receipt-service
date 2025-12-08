package ru.sogaz.site.paymentReceiptService.service.atol

import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignAutoConfiguration
import org.springframework.context.annotation.Import

@Import(
    value = [
        FeignAutoConfiguration::class,
        JacksonAutoConfiguration::class,
        HttpMessageConvertersAutoConfiguration::class,
        ValidationAutoConfiguration::class,
    ],
)
@EnableFeignClients("ru.sogaz.site.paymentReceiptService.clients")
annotation class AtolClientTest
