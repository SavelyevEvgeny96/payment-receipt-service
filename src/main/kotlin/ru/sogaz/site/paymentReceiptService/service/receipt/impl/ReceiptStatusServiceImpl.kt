package ru.sogaz.site.paymentReceiptService.service.receipt.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.CODE_ERROR_UPDATE_STATUS_ID_NOT_FOUND
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentReceiptService.dao.ReceiptDao
import ru.sogaz.site.paymentReceiptService.mapper.web.ResponseMapper
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptStatusRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptUpdateRequest
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptStatusResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptUpdateResponse
import ru.sogaz.site.paymentReceiptService.orThrow
import ru.sogaz.site.paymentReceiptService.service.atol.AtolService
import ru.sogaz.site.paymentReceiptService.service.credentials.CredentialsManager
import ru.sogaz.site.paymentReceiptService.service.receipt.ReceiptStatusService

@Service
@Transactional(rollbackFor = [Exception::class])
class ReceiptStatusServiceImpl(
    private val atolService: AtolService,
    private val receiptDao: ReceiptDao,
    private val responseMapper: ResponseMapper,
    private val credentialsManager: CredentialsManager,
) : ReceiptStatusService {
    companion object {
        private const val EMPTY_EXTERNAL_ID_MESSAGE = "Для чека не указан externalId"
    }

    override fun setStatus(request: PaymentReceiptStatusRequest): PaymentReceiptStatusResponse =
        request.externalId
            .run(receiptDao::findByExternalId)
            .orThrow { BusinessException(CODE_ERROR_UPDATE_STATUS_ID_NOT_FOUND) }
            .apply { state = request.status }
            .run(receiptDao::save)
            .run(responseMapper::toSetStatusResponse)

    override fun updateStatusFromAtol(request: PaymentReceiptUpdateRequest): PaymentReceiptUpdateResponse =
        request.orderId
            .run(receiptDao::findByOrderId)
            .orThrow { BusinessException(CODE_ERROR_UPDATE_STATUS_ID_NOT_FOUND) }
            .run(::updateStatusFromAtol)
            .run(responseMapper::toUpdateStatusResponse)

    override fun updateStatusFromAtol(receipt: Receipt): Receipt =
        receipt
            .apply { state = getAtolReceiptStatus(this) }
            .run(receiptDao::save)

    private fun getAtolReceiptStatus(receipt: Receipt): ReceiptState =
        receipt
            .also(::checkExternalId)
            .run(credentialsManager::findCredentials)
            .run { atolService.getStatus(receipt, this) }

    private fun checkExternalId(receipt: Receipt) {
        if (receipt.externalId == null) {
            throw InnerException(getTraceId(), EMPTY_EXTERNAL_ID_MESSAGE)
        }
    }
}
