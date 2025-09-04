package ru.sogaz.site.paymentReceiptService.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentDocument
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentItem
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest

@Mapper(componentModel = "spring", uses = [PaymentMapperHelper::class])
interface PaymentItemMapper {
    @Mapping(target = "itemId", ignore = true)
    @Mapping(source = "request.name", target = "name")
    @Mapping(source = "request.price", target = "price")
    @Mapping(source = "request.quantity", target = "quantity")
    @Mapping(source = "request.sum", target = "sum")
    @Mapping(source = "request.paymentMethod", target = "paymentMethod", qualifiedByName = ["mapPaymentMethod"])
    @Mapping(source = "request.paymentObject", target = "paymentObject", qualifiedByName = ["mapPaymentObject"])
    @Mapping(source = "request.vat.type", target = "vatType", qualifiedByName = ["mapVatType"])
    @Mapping(source = "document", target = "document")
    @Mapping(target = "dateCreate", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    fun toPaymentItem(
        request: PaymentReceiptCreateRequest.PaymentItemRequest,
        document: PaymentDocument,
    ): PaymentItem
}
