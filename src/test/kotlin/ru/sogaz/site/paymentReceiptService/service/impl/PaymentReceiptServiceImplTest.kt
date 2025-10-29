package ru.sogaz.site.paymentReceiptService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import ru.sogaz.site.paymentReceiptService.mapper.PaymentDocumentMapper
import ru.sogaz.site.paymentReceiptService.mapper.PaymentItemMapper
import ru.sogaz.site.paymentReceiptService.mapper.PaymentReceiptMapper
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentDocument
import ru.sogaz.site.paymentReceiptService.model.reference.ApiVersion
import ru.sogaz.site.paymentReceiptService.model.reference.CheckStatus
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptStatusRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptUpdateRequest
import ru.sogaz.site.paymentReceiptService.repository.PaymentDocumentRepository
import ru.sogaz.site.paymentReceiptService.repository.PaymentItemRepository
import ru.sogaz.site.paymentReceiptService.repository.PaymentReceiptRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.CheckStatusRepository
import ru.sogaz.site.paymentReceiptService.service.AtolClient
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class PaymentReceiptServiceImplTest {
    @Mock
    lateinit var paymentDocumentRepository: PaymentDocumentRepository

    @Mock
    lateinit var paymentItemRepository: PaymentItemRepository

    @Mock
    lateinit var paymentReceiptRepository: PaymentReceiptRepository

    @Mock
    lateinit var checkStatusRepository: CheckStatusRepository

    @Mock
    lateinit var atolClient: AtolClient

    @Mock
    lateinit var paymentDocumentMapper: PaymentDocumentMapper

    @Mock
    lateinit var paymentItemMapper: PaymentItemMapper

    @Mock
    lateinit var paymentReceiptMapper: PaymentReceiptMapper

    @Mock
    lateinit var objectMapper: ObjectMapper

    @InjectMocks
    lateinit var service: PaymentReceiptServiceImpl

    @Test
    fun `createReceipt should return success response`() {
        val request = mock(PaymentReceiptCreateRequest::class.java)
        val document = mock(PaymentDocument::class.java)
        val status = mock(CheckStatus::class.java)
        val apiVersion = mock(ApiVersion::class.java)

        `when`(paymentDocumentMapper.toPaymentDocument(request)).thenReturn(document)
        `when`(document.apiVersion).thenReturn(apiVersion)
        `when`(apiVersion.versionCode).thenReturn("v4")
        `when`(request.items).thenReturn(emptyList())
        `when`(request.payments).thenReturn(emptyList())
        `when`(atolClient.sendAtolRequest(document, emptyList(), emptyList(), "v4")).thenReturn("ext-123")
        `when`(checkStatusRepository.findByStateId("wait")).thenReturn(status)

        val response = service.createReceipt(request)

        assertEquals(1101550200, response.code)
        assertEquals("ext-123", response.data?.externalId)
        assertEquals("SUCCESS", response.status)
    }

    @Test
    fun `updateStatus should return success response`() {
        val payload =
            PaymentReceiptStatusRequest.PaymentCheckPayload(
                ecrRegistrationNumber = "reg123",
                fiscalDocumentAttribute = 123456789L,
                fiscalDocumentNumber = 987654321L,
                fiscalReceiptNumber = 111222333L,
                fnNumber = "fn-001",
                fnsSite = "https://nalog.ru",
                ofdInn = "7701234567",
                receiptDatetime = "2025-05-26T12:00:00",
                shiftNumber = 12,
                total = 999.99,
                ofdReceiptUrl = "https://ofd.com/receipt",
            )

        val request =
            PaymentReceiptStatusRequest(
                callbackUrl = "http://callback",
                daemonCode = "daemon-1",
                deviceCode = "device-1",
                error = null,
                externalId = "ext-123",
                groupCode = "group-1",
                payload = payload,
                status = "done",
                timestamp = "2025-05-26T12:00:00",
                uuid = "uuid-123",
            )

        val document = mock(PaymentDocument::class.java)
        val status = mock(CheckStatus::class.java)

        `when`(paymentDocumentRepository.findByExternalId("ext-123")).thenReturn(document)
        `when`(checkStatusRepository.findByStateId("done")).thenReturn(status)

        val response = service.updateStatus(request)

        assertEquals(1101560200, response.code)
        assertEquals("OK", response.data?.state)
    }

    @Test
    fun `getStatus should return status response`() {
        val request = PaymentReceiptUpdateRequest("ext-123")
        val document = mock(PaymentDocument::class.java)
        val status = CheckStatus(UUID.randomUUID(), "done", "Готово")
        val apiVersion = mock(ApiVersion::class.java)

        `when`(paymentDocumentRepository.findByExternalId("ext-123")).thenReturn(document)
        `when`(document.apiVersion).thenReturn(apiVersion)
        `when`(apiVersion.versionCode).thenReturn("v4")
        `when`(atolClient.getPaymentStatus(request.externalId, "v4")).thenReturn("done")
        `when`(checkStatusRepository.findByStateId("done")).thenReturn(status)

        val response = service.getStatus(request)

        assertEquals(1101520200, response.code)
        assertEquals("done", response.data?.stateId)
        assertEquals("Готово", response.data?.stateName)
    }
}
