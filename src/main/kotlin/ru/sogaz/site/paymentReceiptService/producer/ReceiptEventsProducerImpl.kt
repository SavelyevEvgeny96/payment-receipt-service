package ru.sogaz.site.paymentReceiptService.producer

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentReceiptService.mapper.event.EventMapper
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.properties.RabbitProperties

@Component
class ReceiptEventsProducerImpl(
    private val rabbitTemplate: RabbitTemplate,
    private val rabbitProperties: RabbitProperties,
    private val eventMapper: EventMapper,
) : ReceiptEventsProducer {
    @Transactional(
        propagation = Propagation.SUPPORTS,
        rollbackFor = [Exception::class],
    )
    override fun receiptSentEvent(receipt: Receipt) =
        rabbitTemplate.convertAndSend(
            rabbitProperties.receiptExchange,
            rabbitProperties.receiptSentRoutingKey,
            eventMapper.mapReceiptSentEvent(receipt),
        )

    @Transactional(
        propagation = Propagation.SUPPORTS,
        rollbackFor = [Exception::class],
    )
    override fun receiptCreatedEvent(receipt: Receipt) =
        rabbitTemplate.convertAndSend(
            rabbitProperties.receiptExchange,
            rabbitProperties.receiptCreatedRoutingKey,
            eventMapper.mapReceiptCreatedEvent(receipt),
        )
}
