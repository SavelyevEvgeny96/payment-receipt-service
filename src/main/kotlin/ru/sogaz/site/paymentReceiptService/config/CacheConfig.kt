package ru.sogaz.site.paymentReceiptService.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentReceiptService.repository.reference.ConfigurationDataRepository
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfig(
    private val configurationDataRepository: ConfigurationDataRepository,
) {
    @Bean
    fun cacheManager(): CaffeineCacheManager {
        val tokenTTL =
            configurationDataRepository
                .findByParamName("tokenTime")
                ?.paramValue
                ?.toLong() ?: 60L

        val caffeineCacheManager = CaffeineCacheManager("atolToken")
        caffeineCacheManager.setCaffeine(
            Caffeine
                .newBuilder()
                .expireAfterWrite(tokenTTL, TimeUnit.MINUTES),
        )
        return caffeineCacheManager
    }
}
