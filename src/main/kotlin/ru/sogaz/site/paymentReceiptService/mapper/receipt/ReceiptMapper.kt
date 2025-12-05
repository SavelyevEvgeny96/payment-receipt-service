package ru.sogaz.site.paymentReceiptService.mapper.receipt

import org.jetbrains.kotlin.utils.addToStdlib.butIf
import org.mapstruct.AfterMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest
import java.util.UUID

@Mapper(uses = [ReceiptItemMapper::class, ReceiptPaymentMapper::class], imports = [ReceiptState::class])
abstract class ReceiptMapper {
    @Mapping(target = "state", constant = "NEW")
    @Mapping(target = "depersonalization", defaultValue = "false")
    @Mapping(target = "receiptSystem", source = "system")
    @Mapping(target = "clientEmail", source = "client.email")
    @Mapping(target = "clientPhone", source = "client.phone")
    abstract fun fromCreateRequest(request: PaymentReceiptCreateRequest): Receipt

    @AfterMapping
    protected fun injectReceiptToSubItems(
        @MappingTarget receipt: Receipt,
    ): Receipt =
        receipt.apply {
            items.forEach { it.receipt = this }
            payments.forEach { it.receipt = this }
        }

    fun updateReceiptState(
        receipt: Receipt,
        uuid: UUID?,
    ): Receipt =
        receipt.apply {
            externalId = uuid
            state = ReceiptState.WAIT.butIf(uuid == null) { ReceiptState.FAIL }
        }
}
