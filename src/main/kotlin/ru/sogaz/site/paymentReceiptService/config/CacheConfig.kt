package ru.sogaz.site.paymentReceiptService.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.util.concurrent.TimeUnit

@EnableCaching
@Configuration
class CacheConfig {
    companion object {
        const val ATOL_TOKEN_CACHE = "atolToken"
    }

    @Value("\${config.atol.api.tokenTTL}")
    lateinit var tokenTTL: String

    @Bean
    @Primary
    fun tokenTTLCacheManager(): CaffeineCacheManager =
        CaffeineCacheManager(ATOL_TOKEN_CACHE)
            .apply { setCaffeine(buildCaffeineCache()) }

    private fun buildCaffeineCache() =
        Caffeine
            .newBuilder()
            .expireAfterWrite(tokenTTL.toLong(), TimeUnit.MINUTES)
}
