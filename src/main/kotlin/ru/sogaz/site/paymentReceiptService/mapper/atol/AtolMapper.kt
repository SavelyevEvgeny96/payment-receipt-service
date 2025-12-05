package ru.sogaz.site.paymentReceiptService.mapper.atol

import org.mapstruct.AfterMapping
import org.mapstruct.Context
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingConstants
import org.mapstruct.MappingTarget
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.reference.AtolCredentials
import ru.sogaz.site.paymentReceiptService.model.web.request.atol.AtolRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.atol.AtolTokenRequest
import ru.sogaz.site.paymentReceiptService.properties.AtolProperties
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = [AtolReceiptMapper::class],
)
abstract class AtolMapper {
    abstract fun toTokenRequest(atolCredentials: AtolCredentials): AtolTokenRequest

    @Mapping(target = "externalId", source = "receipt.id")
    @Mapping(target = "service", expression = "java( atolProperties.getCallback() )")
    @Mapping(target = "receipt", source = ".")
    abstract fun mapRequest(
        receipt: Receipt,
        @Context atolProperties: AtolProperties,
    ): AtolRequest

    @AfterMapping
    protected fun fillAtolRequestTimestamp(
        @MappingTarget atolRequest: AtolRequest,
    ): AtolRequest =
        atolRequest.apply {
            timestamp = localDateTimeToFormattedString()
        }

    private fun localDateTimeToFormattedString(): String =
        LocalDateTime
            .now()
            .atZone(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}
