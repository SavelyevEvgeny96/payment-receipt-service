package ru.sogaz.site.paymentReceiptService.service.atol.impl

import feign.FeignException
import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentReceiptService.clients.AtolClient
import ru.sogaz.site.paymentReceiptService.mapper.atol.AtolMapper
import ru.sogaz.site.paymentReceiptService.model.credential.Credentials
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import ru.sogaz.site.paymentReceiptService.properties.AtolProperties
import ru.sogaz.site.paymentReceiptService.service.atol.AtolAuthService
import ru.sogaz.site.paymentReceiptService.service.atol.AtolService
import java.util.UUID

@Service
class AtolServiceImpl(
    private val atolClient: AtolClient,
    private val atolMapper: AtolMapper,
    private val atolAuthService: AtolAuthService,
    private val atolProperties: AtolProperties,
) : AtolService {
    companion object {
        private const val EMPTY_EXTERNAL_ID_MESSAGE = "Для чека не указан externalId"
        private const val MISSING_RECEIPT_TYPE = "Отсутствует тип операции для чека"
    }

    override fun sendReceipt(
        receipt: Receipt,
        credentials: Credentials,
    ): UUID? =
        try {
            val atolToken = atolAuthService.getToken(credentials)
            val atolReceiptType = requireNotNull(receipt.receiptType) { MISSING_RECEIPT_TYPE }
            val atolRequest = atolMapper.mapRequest(receipt, atolProperties)
            atolClient
                .sendReceipt(atolToken, atolReceiptType.desc, atolRequest)
                .uuid
        } catch (ex: FeignException) {
            when (ex.status()) {
                400 -> null
                else -> throw InnerException(getTraceId(), ex.message)
            }
        } catch (ex: Exception) {
            throw InnerException(getTraceId(), ex.message)
        }

    override fun getStatus(
        receipt: Receipt,
        credentials: Credentials,
    ): ReceiptState =
        try {
            val atolToken = atolAuthService.getToken(credentials)
            val externalId = requireNotNull(receipt.externalId) { EMPTY_EXTERNAL_ID_MESSAGE }
            atolClient
                .getStatus(atolToken, externalId)
                .status
        } catch (ex: Exception) {
            throw InnerException(getTraceId(), ex.message)
        }
}
