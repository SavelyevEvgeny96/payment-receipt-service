package ru.sogaz.site.paymentReceiptService.mapper.atol

import org.mapstruct.Context
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import ru.sogaz.site.paymentReceiptService.model.atol.request.AtolItemData
import ru.sogaz.site.paymentReceiptService.model.atol.request.AtolPaymentData
import ru.sogaz.site.paymentReceiptService.model.atol.request.AtolReceiptData
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.entity.ReceiptItem
import ru.sogaz.site.paymentReceiptService.model.entity.ReceiptPayment
import ru.sogaz.site.paymentReceiptService.properties.AtolProperties

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = [AtolCompanyMapper::class],
)
interface AtolReceiptMapper {
    @Mapping(target = "client.email", source = "receipt.clientEmail")
    @Mapping(target = "client.phone", source = "receipt.clientPhone")
    @Mapping(target = "company", source = ".")
    fun mapReceipt(
        receipt: Receipt,
        @Context atolProperties: AtolProperties,
    ): AtolReceiptData

    @Mapping(target = "paymentMethod", source = "paymentMethod.desc")
    @Mapping(target = "paymentObject", source = "paymentObject")
    @Mapping(target = "vat.type", source = "vatType.desc")
    fun mapItem(receiptItem: ReceiptItem): AtolItemData

    @Mapping(target = "type", source = "paymentType.desc")
    fun mapPaymentData(receiptPayment: ReceiptPayment): AtolPaymentData
}
