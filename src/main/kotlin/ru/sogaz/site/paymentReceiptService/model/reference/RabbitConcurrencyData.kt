package ru.sogaz.site.paymentReceiptService.model.reference

data class RabbitConcurrencyData(
    val consumers: Int,
    val maxConsumers: Int,
    val stopConsumerMinIntervalMs: Long,
)
