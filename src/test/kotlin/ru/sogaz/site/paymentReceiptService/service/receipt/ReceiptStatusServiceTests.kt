package ru.sogaz.site.paymentReceiptService.service.receipt

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.paymentReceiptService.dao.ReceiptDao
import ru.sogaz.site.paymentReceiptService.mapper.atol.AtolResponseMapper
import ru.sogaz.site.paymentReceiptService.mapper.atol.AtolResponseMapperImpl
import ru.sogaz.site.paymentReceiptService.model.atol.response.AtolResultResponse
import ru.sogaz.site.paymentReceiptService.model.credential.Credentials
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptStatusRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptUpdateRequest
import ru.sogaz.site.paymentReceiptService.service.atol.AtolService
import ru.sogaz.site.paymentReceiptService.service.credentials.CredentialsManager
import ru.sogaz.site.paymentReceiptService.service.receipt.impl.ReceiptStatusServiceImpl
import java.util.UUID

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Import(value = [AtolResponseMapperImpl::class])
class ReceiptStatusServiceTests {
    companion object {
        private val testCredentials = Credentials("login", "pass")
    }

    @MockK
    private lateinit var atolService: AtolService

    @MockK
    private lateinit var receiptDao: ReceiptDao

    @MockK
    lateinit var credentialsManager: CredentialsManager

    @Autowired
    lateinit var atolResponseMapper: AtolResponseMapper

    private lateinit var receiptStatusService: ReceiptStatusServiceImpl

    @RelaxedMockK
    private lateinit var paymentReceiptStatusRequest: PaymentReceiptStatusRequest

    @RelaxedMockK
    private lateinit var receipt: Receipt

    @MockK
    private lateinit var receiptWithoutExternalId: Receipt

    @RelaxedMockK
    private lateinit var atolResultResponse: AtolResultResponse

    private val validExternalIdUUID: UUID = UUID.randomUUID()
    private val invalidExternalIdUUID: UUID = UUID.randomUUID()

    private val validUpdateRequest = PaymentReceiptUpdateRequest(validExternalIdUUID)

    @BeforeEach
    fun beforeEach() {
        receiptStatusService = initService()

        every { receipt.externalId } returns validExternalIdUUID
        every { receiptDao.findByExternalId(validExternalIdUUID) } returns receipt
        every { receiptDao.findByExternalId(invalidExternalIdUUID) } returns null
        every { receiptDao.findByOrderId(validExternalIdUUID) } returns receipt
        every { receiptDao.findByOrderId(invalidExternalIdUUID) } returns null
        every { receiptDao.save(any()) } returnsArgument 0
        every { credentialsManager.findCredentials(any()) } returns testCredentials
    }

    @Test
    fun `setStatus should update receipt state from request`() {
        every { paymentReceiptStatusRequest.externalId } returns validExternalIdUUID

        receiptStatusService.setStatus(paymentReceiptStatusRequest)

        verify { receipt.state = paymentReceiptStatusRequest.status }
        verify { receiptDao.save(receipt) }
    }

    @Test
    fun `setStatus should thrown an error when invalid externalId`() {
        every { paymentReceiptStatusRequest.externalId } returns invalidExternalIdUUID

        assertThrows<BusinessException> {
            receiptStatusService.setStatus(paymentReceiptStatusRequest)
        }
    }

    @Test
    fun `updateStatusFromAtol should update receipt state by request`() {
        every { atolService.getResult(any(), any()) } returns atolResultResponse

        val receipt = receiptStatusService.updateStatusFromAtol(validUpdateRequest)

        verify { receipt.state = atolResultResponse.status }
        verify { receipt.link = atolResultResponse.payload?.ofdReceiptUrl }
        verify { receiptDao.save(receipt) }
    }

    @Test
    fun `updateStatusFromAtol should update receipt state by receipt`() {
        every { atolService.getResult(any(), any()) } returns atolResultResponse

        receiptStatusService.updateStatusFromAtol(receipt)

        verify { receipt.state = atolResultResponse.status }
        verify { receipt.link = atolResultResponse.payload?.ofdReceiptUrl }
        verify { receiptDao.save(receipt) }
    }

    @Test
    fun `updateStatusFromAtol should thrown an error when receipt without externalId`() {
        every { receipt.externalId } returns null

        assertThrows<IllegalArgumentException> {
            receiptStatusService.updateStatusFromAtol(receipt)
        }

        verify(exactly = 0) { credentialsManager.findCredentials(receipt) }
        verify(exactly = 0) { atolService.getResult(receipt, any()) }
        verify(exactly = 0) { receiptDao.save(receipt) }
    }

    private fun initService() =
        ReceiptStatusServiceImpl(
            atolService = atolService,
            receiptDao = receiptDao,
            credentialsManager = credentialsManager,
            atolResponseMapper = atolResponseMapper,
        )
}
