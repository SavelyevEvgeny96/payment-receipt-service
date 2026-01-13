package ru.sogaz.site.paymentReceiptService.mapper.receipt

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentReceiptService.model.entity.ReceiptPayment
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentPaymentRequest

@Mapper
interface ReceiptPaymentMapper {
    @Mapping(target = "paymentType", source = "type")
    fun toReceiptPayment(requestPayment: PaymentPaymentRequest): ReceiptPayment
}
