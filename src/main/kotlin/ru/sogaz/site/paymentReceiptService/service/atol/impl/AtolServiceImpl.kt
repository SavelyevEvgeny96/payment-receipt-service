package ru.sogaz.site.paymentReceiptService.service.atol.impl

import feign.FeignException
import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentReceiptService.clients.AtolClient
import ru.sogaz.site.paymentReceiptService.mapper.atol.AtolMapper
import ru.sogaz.site.paymentReceiptService.model.atol.request.AtolRequest
import ru.sogaz.site.paymentReceiptService.model.atol.response.AtolResponse
import ru.sogaz.site.paymentReceiptService.model.atol.response.AtolStatusResponse
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import ru.sogaz.site.paymentReceiptService.model.reference.Credentials
import ru.sogaz.site.paymentReceiptService.orThrow
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
    }

    override fun sendReceipt(
        receipt: Receipt,
        credentials: Credentials,
    ): UUID? =
        try {
            receipt
                .run { atolMapper.mapRequest(this, atolProperties) }
                .run { sendReceipt(this, credentials) }
                .uuid
        } catch (ex: FeignException) {
            when (ex.status()) {
                400 -> null
                else -> throw InnerException(getTraceId(), ex.message)
            }
        } catch (ex: Exception) {
            throw InnerException(getTraceId(), ex.message)
        }

    private fun sendReceipt(
        atolRequest: AtolRequest,
        credentials: Credentials,
    ): AtolResponse =
        credentials
            .run(atolMapper::toTokenRequest)
            .run(atolAuthService::getToken)
            .run { atolClient.sendReceipt(this, atolRequest) }

    override fun getStatus(
        receipt: Receipt,
        credentials: Credentials,
    ): ReceiptState =
        try {
            receipt.externalId
                .orThrow { InnerException(getTraceId(), EMPTY_EXTERNAL_ID_MESSAGE) }
                .run { getStatus(this, credentials) }
                .status
        } catch (ex: Exception) {
            throw InnerException(getTraceId(), ex.message)
        }

    private fun getStatus(
        externalId: UUID,
        credentials: Credentials,
    ): AtolStatusResponse =
        credentials
            .run(atolMapper::toTokenRequest)
            .run(atolAuthService::getToken)
            .run { atolClient.getStatus(this, externalId) }
}
