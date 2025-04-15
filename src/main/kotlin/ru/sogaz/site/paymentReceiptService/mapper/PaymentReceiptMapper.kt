package ru.sogaz.site.paymentReceiptService.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentDocument
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentReceipt
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest

@Mapper(componentModel = "spring", uses = [PaymentMapperHelper::class])
interface PaymentReceiptMapper {
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(source = "request.type", target = "paymentType", qualifiedByName = ["mapPaymentType"])
    fun toPaymentReceipt(
        request: PaymentReceiptCreateRequest.PaymentPaymentRequest,
        document: PaymentDocument,
    ): PaymentReceipt
}
