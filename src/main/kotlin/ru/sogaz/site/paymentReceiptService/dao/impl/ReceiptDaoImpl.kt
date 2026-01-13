package ru.sogaz.site.paymentReceiptService.dao.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentReceiptService.dao.ReceiptDao
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import ru.sogaz.site.paymentReceiptService.repository.ReceiptRepository
import java.time.LocalDateTime
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Repository
class ReceiptDaoImpl(
    private val repository: ReceiptRepository,
) : ReceiptDao {
    override fun save(receipt: Receipt): Receipt = repository.save(receipt)

    override fun findById(id: UUID): Receipt? = repository.findById(id).getOrNull()

    override fun findByOrderId(orderId: UUID): Receipt? = repository.findByOrderId(orderId)

    override fun findByExternalId(externalId: UUID): Receipt? = repository.findByExternalId(externalId)

    override fun findByStatusAndDateSendBetween(
        state: ReceiptState,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<Receipt> = repository.findByStateAndDateSendBetween(state, startTime, endTime)
}
