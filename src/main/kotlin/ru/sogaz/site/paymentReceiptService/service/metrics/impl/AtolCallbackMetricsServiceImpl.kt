package ru.sogaz.site.paymentReceiptService.service.metrics.impl

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import ru.sogaz.site.paymentReceiptService.config.MetricsConfig.Companion.ATOL_CALLBACK_FAIL
import ru.sogaz.site.paymentReceiptService.config.MetricsConfig.Companion.DEFAULT_TAG
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptStatusRequest
import ru.sogaz.site.paymentReceiptService.service.metrics.AtolCallbackMetricsService

class AtolCallbackMetricsServiceImpl(
    private val meterRegistry: MeterRegistry,
    private val atolCallbackSuccessCounter: Counter,
) : AtolCallbackMetricsService {
    override fun setMetrics(request: PaymentReceiptStatusRequest) {
        when (request.status) {
            ReceiptState.DONE -> incrementSuccessMetric()
            else -> {
                if (request.error != null) {
                    incrementFailMetric(request.error.text)
                }
            }
        }
    }

    private fun incrementFailMetric(errorText: String) {
        val counter = meterRegistry.counter(ATOL_CALLBACK_FAIL, "ErrorText", errorText ?: DEFAULT_TAG)
        counter.increment()
    }

    private fun incrementSuccessMetric() {
        atolCallbackSuccessCounter.increment()
    }
}
