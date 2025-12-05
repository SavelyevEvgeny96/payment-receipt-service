package ru.sogaz.site.paymentReceiptService.mapper.web

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptCreateResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptStatusResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptUpdateResponse

@Mapper
interface ResponseMapper {
    fun toCreateResponse(receipt: Receipt): PaymentReceiptCreateResponse

    @Mapping(target = "stateId", source = "state.value")
    @Mapping(target = "stateName", source = "state.desc")
    fun toUpdateStatusResponse(receipt: Receipt): PaymentReceiptUpdateResponse

    @Mapping(target = "state", constant = "OK")
    fun toSetStatusResponse(receipt: Receipt): PaymentReceiptStatusResponse
}
