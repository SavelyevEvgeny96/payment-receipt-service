package ru.sogaz.site.paymentReceiptService.mapper.credentials

import org.mapstruct.Mapper
import ru.sogaz.site.paymentReceiptService.model.credential.CredentialsMapping

@Mapper
abstract class CredentialsMapper {
    fun toCredentialsMapping(combination: Pair<String?, String?>): CredentialsMapping =
        CredentialsMapping(
            product = combination.first,
            channel = combination.second,
        )
}
