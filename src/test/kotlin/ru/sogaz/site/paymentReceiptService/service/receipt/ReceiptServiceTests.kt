package ru.sogaz.site.paymentReceiptService.service.receipt

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import ru.sogaz.site.paymentReceiptService.dao.ReceiptDao
import ru.sogaz.site.paymentReceiptService.mapper.receipt.ReceiptMapper
import ru.sogaz.site.paymentReceiptService.mapper.web.ResponseMapper
import ru.sogaz.site.paymentReceiptService.producer.ReceiptEventsProducer
import ru.sogaz.site.paymentReceiptService.service.AtolService

@ExtendWith(MockKExtension::class)
abstract class ReceiptServiceTests {
    @MockK
    lateinit var atolService: AtolService

    @MockK
    lateinit var receiptDao: ReceiptDao

    @MockK
    lateinit var receiptMapper: ReceiptMapper

    @MockK
    lateinit var responseMapper: ResponseMapper

    @RelaxedMockK
    lateinit var receiptEventsProducer: ReceiptEventsProducer

    @InjectMockKs
    lateinit var receiptService: ReceiptServiceImpl
}
