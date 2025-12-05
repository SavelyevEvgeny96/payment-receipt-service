package ru.sogaz.site.paymentReceiptService.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.AcknowledgeMode
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer
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
    companion object {
        const val QUEUE_TYPE = "x-queue-type"
        const val QUORUM = "quorum"

        private const val MAX_RETRY_ATTEMPTS: Int = 100
        private const val MIN_RETRY_INTERVAL: Long = 1_000
        private const val RETRY_MULTIPLAYER: Double = 3.0
        private const val MAX_RETRY_INTERVAL: Long = 600_000
    }

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        jsonConverter: MessageConverter,
    ): RabbitTemplate =
        connectionFactory
            .run(::RabbitTemplate)
            .apply { messageConverter = jsonConverter }

    @Bean
    @Primary
    fun jsonConverter(objectMapper: ObjectMapper): MessageConverter = Jackson2JsonMessageConverter(objectMapper)

    @Bean
    fun receiptExchange(): TopicExchange = TopicExchange(rabbitProperties.receiptExchange, true, false)

    @Bean
    fun paidPaymentsQueue(): Queue =
        QueueBuilder
            .durable(rabbitProperties.receiptRequestedQueue)
            .withArgument(QUEUE_TYPE, QUORUM)
            .build()

    @Bean
    fun sendPaymentChequeQueue(): Queue = QueueBuilder.durable(rabbitProperties.receiptSentQueue).build()

    @Bean
    fun receiptPaidPaymentsBinding(
        receiptExchange: TopicExchange,
        paidPaymentsQueue: Queue,
    ): Binding =
        BindingBuilder
            .bind(paidPaymentsQueue)
            .to(receiptExchange)
            .with(rabbitProperties.receiptRequestedRoutingKey)

    @Bean
    fun receiptSendPaymentChequeBinding(
        receiptExchange: TopicExchange,
        sendPaymentChequeQueue: Queue,
    ): Binding =
        BindingBuilder
            .bind(sendPaymentChequeQueue)
            .to(receiptExchange)
            .with(rabbitProperties.receiptSentRoutingKey)

    @Bean
    fun concurrentContainerFactory(
        connectionFactory: ConnectionFactory,
        jsonConverter: MessageConverter,
        retryInterceptor: RetryOperationsInterceptor,
    ): SimpleRabbitListenerContainerFactory =
        SimpleRabbitListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setMessageConverter(jsonConverter)
            setAdviceChain(retryInterceptor)
            setConcurrentConsumers(rabbitProperties.concurrency.consumers)
            setMaxConcurrentConsumers(rabbitProperties.concurrency.maxConsumers)
            setStopConsumerMinInterval(rabbitProperties.concurrency.stopConsumerMinIntervalMs)
            setAcknowledgeMode(AcknowledgeMode.AUTO)
            setChannelTransacted(true)
        }

    @Bean
    fun retryInterceptor(): RetryOperationsInterceptor =
        RetryInterceptorBuilder
            .stateless()
            .maxAttempts(MAX_RETRY_ATTEMPTS)
            .backOffOptions(
                MIN_RETRY_INTERVAL,
                RETRY_MULTIPLAYER,
                MAX_RETRY_INTERVAL,
            ).build()
}
