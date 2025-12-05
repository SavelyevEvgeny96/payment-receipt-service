package ru.sogaz.site.paymentReceiptService.config

import feign.Client
import feign.Retryer
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.loggingStarter.interceptor.OkHttpLoggingInterceptor
import ru.sogaz.site.loggingStarter.properties.LoggingProperties
import ru.sogaz.site.paymentReceiptService.properties.FeignClientRetryerProperties
import java.util.concurrent.TimeUnit

@Configuration
class FeignClientConfig(
    private val retryerProperties: FeignClientRetryerProperties,
) {
    @Bean
    fun okHttpLoggingInterceptor(loggingProperties: LoggingProperties) = OkHttpLoggingInterceptor(loggingProperties)

    @Bean
    fun okHttpClient(okHttpLoggingInterceptor: OkHttpLoggingInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(okHttpLoggingInterceptor)
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
