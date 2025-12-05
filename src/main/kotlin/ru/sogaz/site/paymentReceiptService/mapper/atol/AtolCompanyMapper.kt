package ru.sogaz.site.paymentReceiptService.mapper.atol

import org.jetbrains.kotlin.utils.addToStdlib.butIf
import org.mapstruct.Context
import org.mapstruct.Mapper
import org.mapstruct.MappingConstants
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.web.request.atol.AtolCompanyData
import ru.sogaz.site.paymentReceiptService.properties.AtolProperties

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
abstract class AtolCompanyMapper {
    fun mapCompanyData(
        receipt: Receipt,
        @Context atolProperties: AtolProperties,
    ): AtolCompanyData =
        with(atolProperties.company) {
            AtolCompanyData(
                email = email,
                inn = inn,
                paymentAddress =
                    paymentAddress.butIf(receipt.depersonalization) {
                        depersonalizedPaymentAddress
                    },
            )
        }
}
