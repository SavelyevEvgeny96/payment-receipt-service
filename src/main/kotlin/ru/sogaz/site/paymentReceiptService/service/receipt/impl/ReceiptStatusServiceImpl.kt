package ru.sogaz.site.paymentReceiptService.service.receipt.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.CODE_ERROR_UPDATE_STATUS_ID_NOT_FOUND
import ru.sogaz.site.paymentReceiptService.dao.ReceiptDao
import ru.sogaz.site.paymentReceiptService.mapper.atol.AtolResponseMapper
import ru.sogaz.site.paymentReceiptService.model.atol.response.AtolResultResponse
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptStatusRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptUpdateRequest
import ru.sogaz.site.paymentReceiptService.orThrow
import ru.sogaz.site.paymentReceiptService.service.atol.AtolService
import ru.sogaz.site.paymentReceiptService.service.credentials.CredentialsManager
import ru.sogaz.site.paymentReceiptService.service.receipt.ReceiptStatusService

@Service
@Transactional(rollbackFor = [Exception::class])
class ReceiptStatusServiceImpl(
    private val receiptDao: ReceiptDao,
    private val atolService: AtolService,
    private val credentialsManager: CredentialsManager,
    private val atolResponseMapper: AtolResponseMapper,
) : ReceiptStatusService {
    companion object {
        private const val EMPTY_EXTERNAL_ID_MESSAGE = "Для чека не указан externalId"
    }

    override fun setStatus(request: PaymentReceiptStatusRequest): Receipt =
        request.externalId
            .run(receiptDao::findByExternalId)
            .orThrow { BusinessException(CODE_ERROR_UPDATE_STATUS_ID_NOT_FOUND) }
            .apply { state = request.status }
            .run(receiptDao::save)

    override fun updateStatusFromAtol(request: PaymentReceiptUpdateRequest): Receipt =
        request.orderId
            .run(receiptDao::findByOrderId)
            .orThrow { BusinessException(CODE_ERROR_UPDATE_STATUS_ID_NOT_FOUND) }
            .run(::updateStatusFromAtol)

    override fun updateStatusFromAtol(receipt: Receipt): Receipt =
        receipt
            .fillFromResult(::getAtolReceiptResult)
            .run(receiptDao::save)

    private fun getAtolReceiptResult(receipt: Receipt): AtolResultResponse =
        receipt
            .also(::checkExternalId)
            .run(credentialsManager::findCredentials)
            .run { atolService.getResult(receipt, this) }

    private fun checkExternalId(receipt: Receipt) {
        requireNotNull(receipt.externalId) { EMPTY_EXTERNAL_ID_MESSAGE }
    }

    private fun Receipt.fillFromResult(getResultBlock: Receipt.() -> AtolResultResponse): Receipt =
        atolResponseMapper.fillReceiptFromResult(this, getResultBlock())
}
