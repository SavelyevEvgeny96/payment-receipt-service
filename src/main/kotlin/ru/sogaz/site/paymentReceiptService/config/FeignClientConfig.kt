package ru.sogaz.site.paymentReceiptService.config

import feign.Client
import feign.Retryer
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentReceiptService.properties.FeignClientRetryerProperties
import java.util.concurrent.TimeUnit

@Configuration
class FeignClientConfig(
    private val retryerProperties: FeignClientRetryerProperties,
) {
    @Bean
    fun okHttpClient(loggingInterceptor: Interceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .build()

    @Bean
    fun feignOkHttpClient(okHttpClient: OkHttpClient): Client = feign.okhttp.OkHttpClient(okHttpClient)

    @Bean
    fun retryer(): Retryer =
        Retryer.Default(
            retryerProperties.minTimeoutMs,
            retryerProperties.maxTimeoutMs,
            retryerProperties.maxRetries,
        )
}
