package ru.sogaz.site.paymentReceiptService.consumer

import jakarta.validation.Valid
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import ru.sogaz.site.exceptionStarter.starter.config.loggerFor
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest
import ru.sogaz.site.paymentReceiptService.service.receipt.ReceiptService

@Validated
@Component
class ReceiptRequestedEventConsumer(
    private val receiptService: ReceiptService,
) {
    companion object {
        private const val CREATE_RECEIPT_ERROR_MESSAGE = "Возникла ошибка при создании чека {}"
        private const val CREATE_RECEIPT_INTEGRITY_ERROR_MESSAGE = "Возникла ошибка уникальности при создании чека для отправки"
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
            receiptService.createReceipt(message)
        } catch (_: DataIntegrityViolationException) {
            logger.warn(CREATE_RECEIPT_INTEGRITY_ERROR_MESSAGE)
        } catch (ex: Exception) {
            logger.error(CREATE_RECEIPT_ERROR_MESSAGE, ex.message, ex)
            throw ex
        }
        }
}
