package ru.sogaz.site.paymentReceiptService.service.atol

import feign.FeignException
import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentReceiptService.clients.AtolClient
import ru.sogaz.site.paymentReceiptService.mapper.atol.AtolMapper
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import ru.sogaz.site.paymentReceiptService.model.web.request.atol.AtolRequest
import ru.sogaz.site.paymentReceiptService.model.web.response.atol.AtolResponse
import ru.sogaz.site.paymentReceiptService.properties.AtolProperties
import ru.sogaz.site.paymentReceiptService.service.AtolAuthService
import ru.sogaz.site.paymentReceiptService.service.AtolService
import java.util.UUID

@Service
class AtolServiceImpl(
    private val atolClient: AtolClient,
    private val atolMapper: AtolMapper,
    private val atolAuthService: AtolAuthService,
    private val atolProperties: AtolProperties,
) : AtolService {
    override fun sendReceipt(receipt: Receipt): UUID? =
        receipt
            .run { atolMapper.mapRequest(receipt, atolProperties) }
            .run(::sendReceipt)
            .uuid

    private fun sendReceipt(atolRequest: AtolRequest): AtolResponse =
        try {
            atolProperties.credentials
                .run(atolAuthService::getToken)
                .run { atolClient.sendReceipt(this, atolRequest) }
        } catch (ex: FeignException) {
            when(ex.status()) {
                400 -> AtolResponse(status = ReceiptState.FAIL.value)
                else -> throw InnerException(getTraceId(), ex.message)
            }
        } catch (ex: Exception) {
            throw InnerException(getTraceId(), ex.message)
        }

    override fun getStatus(externalId: UUID): ReceiptState =
        try {
            atolProperties.credentials
                .run(atolAuthService::getToken)
                .run { atolClient.getStatus(this, externalId) }
                .status
        } catch (ex: Exception) {
            throw InnerException(getTraceId(), ex.message)
        }
}
