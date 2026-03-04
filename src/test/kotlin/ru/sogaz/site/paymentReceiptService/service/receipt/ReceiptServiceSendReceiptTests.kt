package ru.sogaz.site.paymentReceiptService.service.receipt

import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentReceiptService.model.credential.Credentials
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptSystem
import ru.sogaz.site.paymentReceiptService.model.event.ReceiptCreatedEvent
import ru.sogaz.site.paymentReceiptService.model.exception.SendReceiptException
import java.math.BigDecimal
import java.util.UUID

class ReceiptServiceSendReceiptTests : ReceiptServiceTests() {
    companion object {
        private val testCredentials = Credentials("login", "pass")
    }

    private lateinit var receipt: Receipt

    private lateinit var validReceiptUUID: UUID
    private lateinit var invalidReceiptUUID: UUID

    private lateinit var validExternalIdUUID: UUID

    private lateinit var validCreatedEvent: ReceiptCreatedEvent
    private lateinit var invalidCreatedEvent: ReceiptCreatedEvent

    @BeforeEach
    fun beforeEach() {
        receipt = createTestReceipt()

        validReceiptUUID = UUID.randomUUID()
        invalidReceiptUUID = UUID.randomUUID()
        validExternalIdUUID = UUID.randomUUID()

        validCreatedEvent = ReceiptCreatedEvent(validReceiptUUID)
        invalidCreatedEvent = ReceiptCreatedEvent(invalidReceiptUUID)

        every { receiptDao.findById(validReceiptUUID) } returns receipt
        every { receiptDao.save(any()) } returnsArgument 0
        every { credentialsManager.findCredentials(any()) } returns testCredentials
    }

    @Test
    fun `sendReceipt should throw an error when receipt not found`() {
        every { receiptDao.findById(invalidReceiptUUID) } returns null

        assertThrows<SendReceiptException> {
            receiptService.sendReceipt(invalidCreatedEvent)
        }

        verify(exactly = 1) { receiptDao.findById(any()) }
        verify(exactly = 0) { atolService.sendReceipt(any(), any()) }
        verify(exactly = 0) { receiptDao.save(any()) }
    }

    @Test
    fun `sendReceipt shouldn't update receipt state when atolClient thrown an error`() {
        every { atolService.sendReceipt(receipt, testCredentials) } throws mock<InnerException>()

        assertThrows<InnerException> {
            receiptService.sendReceipt(validCreatedEvent)
        }

        verify(exactly = 1) { receiptDao.findById(any()) }
        verify(exactly = 1) { atolService.sendReceipt(any(), testCredentials) }
        verify(exactly = 0) { receiptDao.save(any()) }
    }

    @Test
    fun `sendReceipt should update receipt state when atolClient return externalId`() {
        every { atolService.sendReceipt(receipt, testCredentials) } returns validExternalIdUUID
        every { receiptMapper.updateReceiptState(receipt, validExternalIdUUID) } answers { callOriginal() }

        val sentReceipt = receiptService.sendReceipt(validCreatedEvent)

        sentReceipt
            .run(::assertThat)
            .returns(ReceiptState.WAIT, Receipt::state)

        verify(exactly = 1) { receiptDao.findById(any()) }
        verify(exactly = 1) { atolService.sendReceipt(any(), testCredentials) }
        verify(exactly = 1) { receiptDao.save(receipt) }
    }

    @Test
    fun `sendReceipt should update receipt state when atolClient return answer with errors`() {
        every { atolService.sendReceipt(receipt, testCredentials) } returns null
        every { receiptMapper.updateReceiptState(receipt, null) } answers { callOriginal() }

        val sentReceipt = receiptService.sendReceipt(validCreatedEvent)

        sentReceipt
            .run(::assertThat)
            .returns(ReceiptState.FAIL, Receipt::state)

        verify(exactly = 1) { receiptDao.findById(any()) }
        verify(exactly = 1) { atolService.sendReceipt(any(), testCredentials) }
        verify(exactly = 1) { receiptDao.save(receipt) }
    }

    private fun createTestReceipt() =
        Receipt(
            id = UUID.randomUUID(),
            orderId = UUID.randomUUID(),
            state = ReceiptState.NEW,
            receiptSystem = ReceiptSystem.ATOL,
            externalId = null,
            total = BigDecimal.TEN,
            clientEmail = "",
            clientPhone = "",
            depersonalization = false,
            dateSend = null,
            dateCreate = null,
            dateUpdate = null,
        )
}
