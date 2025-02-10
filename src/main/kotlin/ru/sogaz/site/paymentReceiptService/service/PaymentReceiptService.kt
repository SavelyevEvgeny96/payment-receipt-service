package ru.sogaz.site.paymentReceiptService.service

import org.jboss.logging.MDC
import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.paymentReceiptService.mapper.PaymentDocumentMapper
import ru.sogaz.site.paymentReceiptService.mapper.PaymentItemMapper
import ru.sogaz.site.paymentReceiptService.mapper.PaymentReceiptMapper
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
import ru.sogaz.siter.models.resonses.Response
import java.time.LocalDateTime
import java.util.*

@Service
class PaymentReceiptService(
    private val paymentDocumentRepository: PaymentDocumentRepository,
    private val paymentItemRepository: PaymentItemRepository,
    private val paymentReceiptRepository: PaymentReceiptRepository,
    private val checkStatusRepository: CheckStatusRepository,
    private val atolClient: AtolClient,
    private val paymentDocumentMapper: PaymentDocumentMapper,
    private val paymentItemMapper: PaymentItemMapper,
    private val paymentReceiptMapper: PaymentReceiptMapper,
) {
    fun createCheck(request: PaymentReceiptCreateRequest): Response<PaymentReceiptCreateResponse> {
        val traceId = MDC.get("traceId")?.toString() ?: UUID.randomUUID().toString()

        val document = paymentDocumentMapper.toPaymentDocument(request)
        paymentDocumentRepository.save(document)

        val items = request.items.map { paymentItemMapper.toPaymentItem(it, document) }
        paymentItemRepository.saveAll(items)

        val payments = request.payments.map { paymentReceiptMapper.toPaymentReceipt(it, document) }
        paymentReceiptRepository.saveAll(payments)

        val externalId = atolClient.sendAtolRequest(document, items, payments)

        document.externalId = externalId
        document.dateSend = LocalDateTime.now()
        document.status = checkStatusRepository.findByStateId("wait")
            ?: throw BusinessException(-1101560409)
        paymentDocumentRepository.save(document)

        return Response(
            status = "success",
            code = 1101500200,
            traceId = traceId,
            data = PaymentReceiptCreateResponse("wait", externalId),
        )
    }

    fun updateStatus(request: PaymentReceiptStatusRequest): Response<PaymentReceiptStatusResponse> {
        val traceId = MDC.get("traceId")?.toString() ?: UUID.randomUUID().toString()

        val paymentDocument =
            paymentDocumentRepository.findByExternalId(request.externalId)
                ?: throw BusinessException(-1101560409)

        val newStatus =
            when (request.status) {
                "wait" -> checkStatusRepository.findByStateId("wait")
                "done" -> checkStatusRepository.findByStateId("done")
                "fail" -> checkStatusRepository.findByStateId("fail")
                else -> throw BusinessException(-1101560409)
            }

        paymentDocument.status = newStatus
            ?: throw BusinessException(-1101560409)
        paymentDocument.dateUpdate = LocalDateTime.now()
        paymentDocumentRepository.save(paymentDocument)

        return Response(
            status = "success",
            code = 1101560200,
            traceId = traceId,
            data = PaymentReceiptStatusResponse(state = "OK"),
        )
    }

    fun getStatus(request: PaymentReceiptUpdateRequest): Response<PaymentReceiptUpdateResponse> {
        val traceId = MDC.get("traceId")?.toString() ?: UUID.randomUUID().toString()

        val document =
            paymentDocumentRepository.findByExternalId(request.externalId)
                ?: throw BusinessException(-1101560409)

        val atolStatus = atolClient.getPaymentStatus(document)

        val newStatus =
            checkStatusRepository.findByStateId(atolStatus)
                ?: throw BusinessException(-1101560409)

        document.status = newStatus
        document.dateUpdate = LocalDateTime.now()
        paymentDocumentRepository.save(document)

        return Response(
            status = "success",
            code = 1101560200,
            traceId = traceId,
            data =
                PaymentReceiptUpdateResponse(
                    stateId = atolStatus,
                    stateName = newStatus.stateName,
                ),
        )
    }
}
