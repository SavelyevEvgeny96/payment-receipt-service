package ru.sogaz.site.paymentReceiptService.service.metrics

import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptStatusRequest

interface AtolCallbackMetricsService {
    fun setMetrics(request: PaymentReceiptStatusRequest)
}
