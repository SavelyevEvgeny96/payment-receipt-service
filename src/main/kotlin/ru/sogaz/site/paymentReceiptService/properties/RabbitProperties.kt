package ru.sogaz.site.paymentReceiptService.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.paymentReceiptService.model.reference.RabbitConcurrencyData

@ConfigurationProperties(prefix = "config.rabbit")
class RabbitProperties {
    lateinit var receiptExchange: String
    lateinit var receiptRequestedRoutingKey: String
    lateinit var receiptRequestedQueue: String
    lateinit var receiptRequestedQueueDlq: String
    lateinit var receiptCreatedRoutingKey: String
    lateinit var receiptSentRoutingKey: String
    lateinit var receiptSentQueue: String
    lateinit var concurrency: RabbitConcurrencyData
}
