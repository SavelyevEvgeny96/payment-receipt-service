package ru.sogaz.site.paymentReceiptService.model.web.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import ru.sogaz.site.paymentReceiptService.config.ValidatorConfig.Companion.CURRENCY_MAX
import ru.sogaz.site.paymentReceiptService.model.enums.ApiVersion
import ru.sogaz.site.paymentReceiptService.model.enums.PaymentMethod
import ru.sogaz.site.paymentReceiptService.model.enums.PaymentType
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptSystem
import ru.sogaz.site.paymentReceiptService.model.enums.VatType
import ru.sogaz.site.paymentReceiptService.validation.constraint.Email
import ru.sogaz.site.paymentReceiptService.validation.constraint.Name
import ru.sogaz.site.paymentReceiptService.validation.constraint.PatternCurrency
import ru.sogaz.site.paymentReceiptService.validation.constraint.PatternQuantity
import ru.sogaz.site.paymentReceiptService.validation.constraint.Phone
import java.math.BigDecimal
import java.util.UUID

data class PaymentReceiptCreateRequest(
    val client: ClientInfo,
    val orderId: UUID,
    @param:NotEmpty
    val items: List<PaymentItemRequest> = emptyList(),
    @param:NotEmpty
    val payments: List<PaymentPaymentRequest> = emptyList(),
    @param:PatternCurrency
    @param:Max(value = CURRENCY_MAX)
    val total: BigDecimal,
    val depersonalization: Boolean = false,
    val system: ReceiptSystem,
    val version: ApiVersion,
) {
    data class ClientInfo(
        @param:Email
        val email: String,
        @param:Phone
        val phone: String?,
        @param:Name
        val name: String?,
    )

    data class PaymentItemRequest(
        @param:Size(max = 128)
        val name: String,
        @param:PatternCurrency
        @param:Max(value = CURRENCY_MAX)
        val price: BigDecimal,
        @param:PatternQuantity
        val quantity: BigDecimal,
        @param:PatternCurrency
        @param:Max(value = CURRENCY_MAX)
        val sum: BigDecimal,
        val paymentMethod: PaymentMethod,
        val paymentObject: String,
        val vat: VatRequest,
    )

    data class VatRequest(
        val type: VatType,
    )

    data class PaymentPaymentRequest(
        val type: PaymentType,
        @param:PatternCurrency
        @param:Max(value = CURRENCY_MAX)
        val sum: BigDecimal,
    )
}
