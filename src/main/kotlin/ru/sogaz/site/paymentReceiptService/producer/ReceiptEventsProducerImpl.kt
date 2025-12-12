package ru.sogaz.site.paymentReceiptService.producer

import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentReceiptService.mapper.event.EventMapper
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.properties.RabbitProperties

@Component
@Transactional(rollbackFor = [Exception::class])
class ReceiptEventsProducerImpl(
    private val rabbitTemplate: RabbitTemplate,
    private val rabbitProperties: RabbitProperties,
    private val eventMapper: EventMapper,
) : ReceiptEventsProducer {
    override fun receiptSentEvent(receipt: Receipt) =
        rabbitTemplate.convertAndSend(
            rabbitProperties.receiptExchange,
            rabbitProperties.receiptSentRoutingKey,
            eventMapper.mapReceiptSentEvent(receipt),
            CorrelationData(receipt.orderId.toString()),
        )

    override fun receiptCreatedEvent(receipt: Receipt) =
        rabbitTemplate.convertAndSend(
            rabbitProperties.receiptExchange,
            rabbitProperties.receiptCreatedRoutingKey,
            eventMapper.mapReceiptCreatedEvent(receipt),
            CorrelationData(receipt.orderId.toString()),
        )
}
