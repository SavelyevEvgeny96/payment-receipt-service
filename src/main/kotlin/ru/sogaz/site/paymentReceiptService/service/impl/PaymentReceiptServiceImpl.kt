package ru.sogaz.site.paymentReceiptService.service.impl

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.CODE_ERROR_UPDATE_STATUS_ID_NOT_FOUND
import ru.sogaz.site.filterStarter.util.TraceId
import ru.sogaz.site.paymentReceiptService.mapper.PaymentDocumentMapper
import ru.sogaz.site.paymentReceiptService.mapper.PaymentItemMapper
import ru.sogaz.site.paymentReceiptService.mapper.PaymentReceiptMapper
import ru.sogaz.site.paymentReceiptService.model.enums.DocumentStatus
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptStatusRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptUpdateRequest
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptCreateResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptStatusResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptUpdateResponse
import ru.sogaz.site.paymentReceiptService.repository.PaymentDocumentRepository
import ru.sogaz.site.paymentReceiptService.repository.PaymentItemRepository
import ru.sogaz.site.paymentReceiptService.repository.PaymentReceiptRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.CheckStatusRepository
import ru.sogaz.site.paymentReceiptService.service.AtolClient
import ru.sogaz.site.paymentReceiptService.service.PaymentReceiptService
import ru.sogaz.siter.models.resonses.Response
import ru.sogaz.siter.models.resonses.getSuccessResponse
import java.time.LocalDateTime

class PaymentReceiptServiceImpl(
    private val paymentDocumentRepository: PaymentDocumentRepository,
    private val paymentItemRepository: PaymentItemRepository,
    private val paymentReceiptRepository: PaymentReceiptRepository,
    private val checkStatusRepository: CheckStatusRepository,
    private val atolClient: AtolClient,
    private val paymentDocumentMapper: PaymentDocumentMapper,
    private val paymentItemMapper: PaymentItemMapper,
    private val paymentReceiptMapper: PaymentReceiptMapper,
) : PaymentReceiptService {
    companion object {
        const val CREATE_RECEIPT_CODE_SUCCESS = 1101550200
        const val UPDATE_STATUS_CODE_SUCCESS = 1101560200
        const val GET_STATUS_CODE_SUCCESS = 1101520200

        const val STATUS_NOT_FOUND = "Status not found"
        const val PAYMENT_DOCUMENT_NOT_FOUND = "Payment document not found"
    }

    override fun createReceipt(request: PaymentReceiptCreateRequest): Response<PaymentReceiptCreateResponse> {
        val traceId = TraceId.get()

        val document = paymentDocumentMapper.toPaymentDocument(request)
        paymentDocumentRepository.save(document)

        val items = request.items.map { paymentItemMapper.toPaymentItem(it, document) }
        paymentItemRepository.saveAll(items)

        val payments = request.payments.map { paymentReceiptMapper.toPaymentReceipt(it, document) }
        paymentReceiptRepository.saveAll(payments)

        val externalId = atolClient.sendAtolRequest(document, items, payments)

        document.externalId = externalId
        document.dateSend = LocalDateTime.now()
        document.status = checkStatusRepository.findByStateId(DocumentStatus.WAIT.toString())
            ?: throw InnerException(traceId, STATUS_NOT_FOUND)
        paymentDocumentRepository.save(document)

        return getSuccessResponse(
            traceId,
            CREATE_RECEIPT_CODE_SUCCESS,
            PaymentReceiptCreateResponse(DocumentStatus.WAIT.toString(), externalId),
        )
    }

    override fun updateStatus(request: PaymentReceiptStatusRequest): Response<PaymentReceiptStatusResponse> {
        val traceId = TraceId.get()

        val paymentDocument =
            paymentDocumentRepository.findByExternalId(request.externalId)
                ?: throw BusinessException(CODE_ERROR_UPDATE_STATUS_ID_NOT_FOUND, traceId)

        val newStatus =
            when (request.status) {
                "wait" -> checkStatusRepository.findByStateId(DocumentStatus.WAIT.toString())
                "done" -> checkStatusRepository.findByStateId(DocumentStatus.DONE.toString())
                "fail" -> checkStatusRepository.findByStateId(DocumentStatus.FAIL.toString())
                else -> throw InnerException(traceId, STATUS_NOT_FOUND)
            }

        paymentDocument.status = newStatus
            ?: throw InnerException(traceId, STATUS_NOT_FOUND)
        paymentDocument.dateUpdate = LocalDateTime.now()
        paymentDocumentRepository.save(paymentDocument)

        return getSuccessResponse(traceId, UPDATE_STATUS_CODE_SUCCESS, PaymentReceiptStatusResponse(state = "OK"))
    }

    override fun getStatus(request: PaymentReceiptUpdateRequest): Response<PaymentReceiptUpdateResponse> {
        val traceId = TraceId.get()

        val document =
            paymentDocumentRepository.findByExternalId(request.externalId)
                ?: throw InnerException(traceId, PAYMENT_DOCUMENT_NOT_FOUND)

        val atolStatus = atolClient.getPaymentStatus(document)

        val newStatus =
            checkStatusRepository.findByStateId(atolStatus)
                ?: throw InnerException(traceId, STATUS_NOT_FOUND)

        document.status = newStatus
        document.dateUpdate = LocalDateTime.now()
        paymentDocumentRepository.save(document)

        return getSuccessResponse(
            traceId,
            GET_STATUS_CODE_SUCCESS,
            PaymentReceiptUpdateResponse(
                stateId = atolStatus,
                stateName = newStatus.stateName,
            ),
        )
    }
}
