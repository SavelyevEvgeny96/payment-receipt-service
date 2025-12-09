package ru.sogaz.site.paymentReceiptService.service.receipt

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentReceiptService.dao.ReceiptDao
import ru.sogaz.site.paymentReceiptService.mapper.receipt.ReceiptMapper
import ru.sogaz.site.paymentReceiptService.mapper.web.ResponseMapper
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.event.ReceiptCreatedEvent
import ru.sogaz.site.paymentReceiptService.model.exception.SendReceiptException
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptCreateResponse
import ru.sogaz.site.paymentReceiptService.orThrow
import ru.sogaz.site.paymentReceiptService.producer.ReceiptEventsProducer
import ru.sogaz.site.paymentReceiptService.service.AtolService
import ru.sogaz.site.paymentReceiptService.service.ReceiptService

@Service
@Transactional(rollbackFor = [Exception::class])
class ReceiptServiceImpl(
    private val atolService: AtolService,
    private val receiptDao: ReceiptDao,
    private val receiptMapper: ReceiptMapper,
    private val responseMapper: ResponseMapper,
    private val receiptEventsProducer: ReceiptEventsProducer,
) : ReceiptService {
    companion object {
        private const val RECEIPT_NOT_FOUND_EXCEPTION_MESSAGE = "Не удалось найти чек на отправку по id [%s]"
    }

    override fun createReceipt(receipt: Receipt): PaymentReceiptCreateResponse =
        receipt
            .run(receiptDao::save)
            .also(receiptEventsProducer::receiptCreatedEvent)
            .run(responseMapper::toCreateResponse)

    override fun sendReceipt(receiptCreatedEvent: ReceiptCreatedEvent): Receipt =
        receiptCreatedEvent.id
            .run(receiptDao::findById)
            .orThrow { notFoundException(receiptCreatedEvent) }
            .run(::sendReceipt)

    private fun notFoundException(receiptCreatedRequest: ReceiptCreatedEvent): SendReceiptException =
        SendReceiptException(RECEIPT_NOT_FOUND_EXCEPTION_MESSAGE.format(receiptCreatedRequest.id))

    private fun sendReceipt(receipt: Receipt): Receipt =
        receipt
            .run(atolService::sendReceipt)
            .run { receiptMapper.updateReceiptState(receipt, this) }
            .run(receiptDao::save)
}
