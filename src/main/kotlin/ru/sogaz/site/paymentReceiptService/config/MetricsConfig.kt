package ru.sogaz.site.paymentReceiptService.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentReceiptService.service.metrics.impl.AtolCallbackMetricsServiceImpl

@Configuration
class MetricsConfig {
    companion object {
        const val ATOL_CALLBACK_FAIL = "gpb_callback_fail"
        const val ATOL_CALLBACK_SUCCESS = "gpb_callback_success"
        const val SUCCESS = "success"
        const val FAIL = "fail"
        const val DEFAULT_TAG = "default"
    }

    @Bean
    fun atolCallbackMetricsService(meterRegistry: MeterRegistry): AtolCallbackMetricsServiceImpl {
        val atolCallbackSuccessCounter: Counter = meterRegistry.counter(ATOL_CALLBACK_SUCCESS)
        return AtolCallbackMetricsServiceImpl(meterRegistry, atolCallbackSuccessCounter)
    }
}
