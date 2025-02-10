package ru.sogaz.site.paymentReceiptService.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentDocument
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest

@Mapper(componentModel = "spring", uses = [PaymentMapperHelper::class])
interface PaymentDocumentMapper {
    @Mapping(target = "docId", ignore = true)
    @Mapping(source = "client.email", target = "clientEmail")
    @Mapping(source = "client.phone", target = "clientPhone")
    @Mapping(source = "client.name", target = "clientName")
    @Mapping(source = "total", target = "total")
    @Mapping(source = "system", target = "cashRegister", qualifiedByName = ["mapSystem"])
    @Mapping(source = "version", target = "apiVersion", qualifiedByName = ["mapVersion"])
    @Mapping(target = "status", expression = "java(paymentHelper.getInitialStatus())")
    fun toPaymentDocument(request: PaymentReceiptCreateRequest): PaymentDocument
}
