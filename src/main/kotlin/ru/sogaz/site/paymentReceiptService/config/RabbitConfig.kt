package ru.sogaz.site.paymentReceiptService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.ConfirmType.NONE
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.retry.interceptor.RetryOperationsInterceptor
import ru.sogaz.site.paymentReceiptService.properties.RabbitProperties

@EnableRabbit
@Configuration
class RabbitConfig(
    private val rabbitProperties: RabbitProperties,
) {
    @Bean
    fun rabbitTemplate(
        connectionFactory: CachingConnectionFactory,
        jsonConverter: MessageConverter,
    ): RabbitTemplate =
        connectionFactory
            .apply { setPublisherConfirmType(NONE) }
            .run(::RabbitTemplate)
            .apply {
                messageConverter = jsonConverter
                isChannelTransacted = true
            }

    @Bean
    @Primary
    fun jsonConverter(objectMapper: ObjectMapper): MessageConverter = Jackson2JsonMessageConverter(objectMapper)

    @Bean
    fun receiptExchange(): TopicExchange = TopicExchange(rabbitProperties.receiptExchange)

    @Bean
    fun receiptRequestedQueue(): Queue =
        QueueBuilder
            .durable(rabbitProperties.receiptRequestedQueue)
            .quorum()
            .deadLetterExchange(rabbitProperties.receiptExchange)
            .deadLetterRoutingKey(rabbitProperties.receiptRequestedRoutingKeyDlq)
            .build()

    @Bean
    fun receiptRequestedQueueDlq(): Queue =
        QueueBuilder
            .durable(rabbitProperties.receiptRequestedQueueDlq)
            .quorum()
            .build()

    @Bean
    fun receiptCreatedQueue(): Queue =
        QueueBuilder
            .durable(rabbitProperties.receiptCreatedQueue)
            .quorum()
            .build()

    @Bean
    fun receiptSentQueue(): Queue =
        QueueBuilder
            .durable(rabbitProperties.receiptSentQueue)
            .quorum()
            .build()

    @Bean
    fun receiptRequestedQueueBinding(
        receiptExchange: TopicExchange,
        receiptRequestedQueue: Queue,
    ): Binding =
        BindingBuilder
            .bind(receiptRequestedQueue)
            .to(receiptExchange)
            .with(rabbitProperties.receiptRequestedRoutingKey)

    @Bean
    fun receiptRequestedQueueDlqBinding(
        receiptExchange: TopicExchange,
        receiptRequestedQueueDlq: Queue,
    ): Binding =
        BindingBuilder
            .bind(receiptRequestedQueueDlq)
            .to(receiptExchange)
            .with(rabbitProperties.receiptRequestedRoutingKeyDlq)

    @Bean
    fun receiptCreatedQueueBinding(
        receiptExchange: TopicExchange,
        receiptCreatedQueue: Queue,
    ): Binding =
        BindingBuilder
            .bind(receiptCreatedQueue)
            .to(receiptExchange)
            .with(rabbitProperties.receiptCreatedRoutingKey)

    @Bean
    fun receiptSentQueueBinding(
        receiptExchange: TopicExchange,
        receiptSentQueue: Queue,
    ): Binding =
        BindingBuilder
            .bind(receiptSentQueue)
            .to(receiptExchange)
            .with(rabbitProperties.receiptSentRoutingKey)

    @Bean
    fun concurrentContainerFactory(
        connectionFactory: ConnectionFactory,
        jsonConverter: MessageConverter,
    ): SimpleRabbitListenerContainerFactory =
        SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setMessageConverter(jsonConverter)
            setChannelTransacted(true)
            setConcurrentConsumers(rabbitProperties.concurrency.consumers)
            setMaxConcurrentConsumers(rabbitProperties.concurrency.maxConsumers)
            setStopConsumerMinInterval(rabbitProperties.concurrency.stopConsumerMinIntervalMs)
        }
}
