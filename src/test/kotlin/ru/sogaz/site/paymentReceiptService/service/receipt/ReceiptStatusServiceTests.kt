package ru.sogaz.site.paymentReceiptService.service.receipt

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentReceiptService.dao.ReceiptDao
import ru.sogaz.site.paymentReceiptService.mapper.web.ResponseMapperImpl
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptSystem
import ru.sogaz.site.paymentReceiptService.model.reference.Credentials
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptStatusRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptUpdateRequest
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptUpdateResponse
import ru.sogaz.site.paymentReceiptService.service.atol.AtolService
import ru.sogaz.site.paymentReceiptService.service.credentials.CredentialsManager
import ru.sogaz.site.paymentReceiptService.service.receipt.impl.ReceiptStatusServiceImpl
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Import(value = [ResponseMapperImpl::class])
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
    private lateinit var responseMapper: ResponseMapperImpl

    private lateinit var receiptStatusService: ReceiptStatusServiceImpl

    @RelaxedMockK
    private lateinit var paymentReceiptStatusRequest: PaymentReceiptStatusRequest

    private lateinit var receipt: Receipt
    private lateinit var receiptWithoutExternalId: Receipt

    private val validExternalIdUUID: UUID = UUID.randomUUID()
    private val invalidExternalIdUUID: UUID = UUID.randomUUID()

    private val validUpdateRequest = PaymentReceiptUpdateRequest(validExternalIdUUID)

    @BeforeEach
    fun beforeEach() {
        receiptStatusService = initService()

        receipt = createValidTestReceipt()
        receiptWithoutExternalId = createInvalidTestReceipt()

        every { receiptDao.findByExternalId(validExternalIdUUID) } returns receipt
        every { receiptDao.findByExternalId(invalidExternalIdUUID) } returns null
        every { receiptDao.findByOrderId(validExternalIdUUID) } returns receipt
        every { receiptDao.findByOrderId(invalidExternalIdUUID) } returns null
        every { receiptDao.save(any()) } returnsArgument 0
        every { credentialsManager.findCredentials(any()) } returns testCredentials
    }

    @Test
    fun `setStatus should update receipt state from request`() {
        val receiptSlot = slot<Receipt>()
        val state = ReceiptState.DONE
        every { paymentReceiptStatusRequest.status } returns state
        every { paymentReceiptStatusRequest.externalId } returns validExternalIdUUID

        receiptStatusService.setStatus(paymentReceiptStatusRequest)

        verify { receiptDao.save(capture(receiptSlot)) }
        assertThat(receiptSlot.captured)
            .returns(state, Receipt::state)
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
        val receiptSlot = slot<Receipt>()
        val state = ReceiptState.DONE
        every { atolService.getStatus(any(), any()) } returns state

        val response = receiptStatusService.updateStatusFromAtol(validUpdateRequest)

        assertThat(response)
            .returns(state.value, PaymentReceiptUpdateResponse::stateId)
            .returns(state.desc, PaymentReceiptUpdateResponse::stateName)

        verify { receiptDao.save(capture(receiptSlot)) }
        assertThat(receiptSlot.captured)
            .returns(state, Receipt::state)
    }

    @Test
    fun `updateStatusFromAtol should update receipt state by receipt`() {
        val state = ReceiptState.DONE
        every { atolService.getStatus(any(), any()) } returns state

        val updatedReceipt = receiptStatusService.updateStatusFromAtol(receipt)

        assertThat(updatedReceipt)
            .returns(state, Receipt::state)
    }

    @Test
    fun `updateStatusFromAtol should thrown an error when receipt without externalId`() {
        assertThrows<InnerException> {
            receiptStatusService.updateStatusFromAtol(receiptWithoutExternalId)
        }
    }

    private fun createValidTestReceipt() =
        createTestReceipt()
            .apply {
                externalId = validExternalIdUUID
            }

    private fun createInvalidTestReceipt() =
        createTestReceipt()
            .apply {
                externalId = null
            }

    private fun createTestReceipt() =
        Receipt(
            id = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            state = ReceiptState.NEW,
            receiptSystem = ReceiptSystem.ATOL,
            externalId = validExternalIdUUID,
            total = BigDecimal.TEN,
            clientEmail = "",
            clientPhone = "",
            depersonalization = false,
            dateSend = null,
            dateCreate = null,
            dateUpdate = null,
        )

    private fun initService() =
        ReceiptStatusServiceImpl(
            atolService = atolService,
            receiptDao = receiptDao,
            responseMapper = responseMapper,
            credentialsManager = credentialsManager,
        )
}
