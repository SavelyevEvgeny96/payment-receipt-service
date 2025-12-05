package ru.sogaz.site.paymentReceiptService.mapper.receipt

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentReceiptService.model.entity.ReceiptItem
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest.PaymentItemRequest

@Mapper
interface ReceiptItemMapper {
    @Mapping(target = "vatType", source = "vat.type")
    fun toReceiptItem(requestItem: PaymentItemRequest): ReceiptItem
}
