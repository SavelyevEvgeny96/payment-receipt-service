package ru.sogaz.site.paymentReceiptService.consumer

import jakarta.validation.Valid
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import ru.sogaz.site.exceptionStarter.starter.config.loggerFor
import ru.sogaz.site.paymentReceiptService.mapper.receipt.ReceiptMapper
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest
import ru.sogaz.site.paymentReceiptService.service.receipt.ReceiptService

@Validated
@Component
class ReceiptRequestedEventConsumer(
    private val receiptService: ReceiptService,
    private val receiptMapper: ReceiptMapper,
) {
    companion object {
        private const val CREATE_RECEIPT_ERROR_MESSAGE = "Возникла ошибка уникальности при создании чека для отправки"
    }

    private val logger = loggerFor(javaClass)

    @RabbitListener(
        queues = ["\${config.rabbit.receiptRequestedQueue}"],
        containerFactory = "concurrentContainerFactory",
    )
    fun onMessage(
        @Valid message: PaymentReceiptCreateRequest,
    ) {
        try {
            message
                .run(receiptMapper::fromCreateRequest)
                .run(receiptService::createReceipt)
        } catch (ex: DataIntegrityViolationException) {
            logger.warn(CREATE_RECEIPT_ERROR_MESSAGE, ex)
        }
    }
}
