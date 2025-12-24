package ru.sogaz.site.paymentReceiptService.mapper.receipt

import org.mapstruct.Mapper
import ru.sogaz.site.paymentReceiptService.model.entity.ReceiptItem
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentItemRequest

@Mapper
interface ReceiptItemMapper {
    fun toReceiptItem(requestItem: PaymentItemRequest): ReceiptItem
}
