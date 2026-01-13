package ru.sogaz.site.paymentReceiptService.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import kotlin.properties.Delegates

@ConfigurationProperties(prefix = "config.feign.retryer")
class FeignClientRetryerProperties {
    var maxRetries by Delegates.notNull<Int>()
    var minTimeoutMs by Delegates.notNull<Long>()
    var maxTimeoutMs by Delegates.notNull<Long>()
}
