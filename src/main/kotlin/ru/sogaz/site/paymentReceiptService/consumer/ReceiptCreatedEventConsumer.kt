package ru.sogaz.site.paymentReceiptService.consumer

import jakarta.validation.Valid
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import ru.sogaz.site.exceptionStarter.starter.config.loggerFor
import ru.sogaz.site.paymentReceiptService.model.event.ReceiptCreatedEvent
import ru.sogaz.site.paymentReceiptService.model.exception.SendReceiptException
import ru.sogaz.site.paymentReceiptService.service.ReceiptService

@Validated
@Component
class ReceiptCreatedEventConsumer(
    private val receiptService: ReceiptService,
) {
    companion object {
        private const val SEND_RECEIPT_ERROR_MESSAGE = "Возникла ошибка при отправке чека"
    }

    private val logger = loggerFor(javaClass)

    @RabbitListener(
        queues = ["\${config.rabbit.receiptCreatedQueue}"],
        containerFactory = "concurrentContainerFactory",
    )
    fun onMessage(
        @Valid message: ReceiptCreatedEvent,
    ) {
        try {
            receiptService.sendReceipt(message)
        } catch (ex: SendReceiptException) {
            logger.warn(SEND_RECEIPT_ERROR_MESSAGE, ex)
        }
    }
}
