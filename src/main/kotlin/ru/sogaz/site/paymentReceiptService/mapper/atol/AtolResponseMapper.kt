package ru.sogaz.site.paymentReceiptService.mapper.atol

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import ru.sogaz.site.paymentReceiptService.model.atol.response.AtolResultResponse
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt

@Mapper
interface AtolResponseMapper {
    @Mapping(target = "state", source = "status")
    @Mapping(target = "link", source = "payload.ofdReceiptUrl")
    @Mapping(target = "sendingTime", source = "payload.receiptDatetime")
    fun fillReceiptFromResult(
        @MappingTarget receipt: Receipt,
        atolResultResponse: AtolResultResponse,
    ): Receipt
}
