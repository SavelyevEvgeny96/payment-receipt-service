package ru.sogaz.site.paymentReceiptService.service.receipt

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest

class ReceiptServiceCreateReceiptTests : ReceiptServiceTests() {
    @RelaxedMockK
    private lateinit var receipt: Receipt

    @RelaxedMockK
    private lateinit var requests: PaymentReceiptCreateRequest

    @BeforeEach
    fun beforeEach() {
        every { receiptMapper.fromCreateRequest(requests) } returns receipt
        every { receiptDao.save(receipt) } returnsArgument 0
        every { responseMapper.toCreateResponse(receipt) } returns mock()
    }

    @Test
    fun `createReceipt should save entity end send event`() {
        receiptService.createReceipt(requests)

        verify(exactly = 1) { receiptDao.save(any()) }
        verify(exactly = 1) { receiptEventsProducer.receiptCreatedEvent(any()) }
    }

    @Test
    fun `createReceipt shouldn't send event if receipt saving thrown an error`() {
        every { receiptDao.save(receipt) } throws mock<InnerException>()

        assertThrows<InnerException> {
            receiptService.createReceipt(requests)
        }

        verify(exactly = 1) { receiptDao.save(any()) }
        verify(exactly = 0) { receiptEventsProducer.receiptCreatedEvent(any()) }
    }
}
