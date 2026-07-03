package ru.sogaz.site.paymentReceiptService.taxcom.config

import feign.Client
import feign.Request
import feign.RequestInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import org.springframework.context.annotation.Bean
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

class TaxcomFeignConfig {
    @Bean
    fun taxcomFeignClient(
        taxcomProperties: TaxcomProperties,
        loggingInterceptor: Interceptor,
    ): Client {
        val okHttpClientBuilder = OkHttpClient
            .Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        configureProxy(taxcomProperties.proxy, okHttpClientBuilder)

        return feign.okhttp.OkHttpClient(okHttpClientBuilder.build())
    }

    @Bean
    fun taxcomRequestInterceptor(): RequestInterceptor = RequestInterceptor { template ->
        template.header("Accept", "application/json")
        template.header("User-Agent", "curl/8.5.0")
        if (template.body() != null) {
            template.header("Content-Type", "application/json")
        }
    }

    @Bean
    fun taxcomFeignOptions(): Request.Options =
        Request.Options(
            30,
            TimeUnit.SECONDS,
            60,
            TimeUnit.SECONDS,
            true,
        )

    private fun configureProxy(proxy: TaxcomProperties.Proxy, okHttpClientBuilder: OkHttpClient.Builder) {
        if (!proxy.enabled) {
            return
        }

        require(proxy.host.isNotBlank()) { "Taxcom proxy host must not be blank when proxy is enabled" }
        require(proxy.port > 0) { "Taxcom proxy port must be positive when proxy is enabled" }

        okHttpClientBuilder.proxy(
            Proxy(
                Proxy.Type.HTTP,
                InetSocketAddress(proxy.host, proxy.port),
            ),
        )
    }
}
