package ru.sogaz.site.paymentReceiptService.mapper.event

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.event.ReceiptCreatedEvent
import ru.sogaz.site.paymentReceiptService.model.event.ReceiptSentEvent

@Mapper
interface EventMapper {
    fun mapReceiptCreatedEvent(receipt: Receipt): ReceiptCreatedEvent

    @Mapping(target = "receiptId", source = "id")
    @Mapping(target = "amount", source = "total")
    @Mapping(target = "typeOperation", source = "receiptType")
    fun mapReceiptSentEvent(receipt: Receipt): ReceiptSentEvent
}
