package ru.sogaz.site.paymentReceiptService.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentReceiptService.properties.ConfigurationDataProperties
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfig(
    private val configurationDataProperties: ConfigurationDataProperties,
) {
    @Bean
    fun cacheManager(): CaffeineCacheManager {
        val tokenTTL = configurationDataProperties.tokenTime.toLong()

        val caffeineCacheManager = CaffeineCacheManager("atolToken")
        caffeineCacheManager.setCaffeine(
            Caffeine
                .newBuilder()
                .expireAfterWrite(tokenTTL, TimeUnit.MINUTES),
        )
        return caffeineCacheManager
    }
}
