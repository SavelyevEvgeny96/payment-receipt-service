package ru.sogaz.site.paymentReceiptService.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentDocument
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentItem
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest

@Mapper(componentModel = "spring", uses = [PaymentMapperHelper::class])
interface PaymentItemMapper {
    @Mapping(target = "itemId", ignore = true)
    @Mapping(source = "paymentMethod", target = "paymentMethod", qualifiedByName = ["mapPaymentMethod"])
    @Mapping(source = "paymentObject", target = "paymentObject", qualifiedByName = ["mapPaymentObject"])
    @Mapping(source = "vat.type", target = "vatType", qualifiedByName = ["mapVatType"])
    fun toPaymentItem(
        request: PaymentReceiptCreateRequest.PaymentItemRequest,
        document: PaymentDocument,
    ): PaymentItem
}
