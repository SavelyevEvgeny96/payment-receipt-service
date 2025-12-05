package ru.sogaz.site.paymentReceiptService.mapper.event

import org.mapstruct.Mapper
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.event.ReceiptCreatedEvent
import ru.sogaz.site.paymentReceiptService.model.event.ReceiptSentEvent

@Mapper
interface EventMapper {
    fun mapReceiptCreatedEvent(receipt: Receipt): ReceiptCreatedEvent

    fun mapReceiptSentEvent(receipt: Receipt): ReceiptSentEvent
}
