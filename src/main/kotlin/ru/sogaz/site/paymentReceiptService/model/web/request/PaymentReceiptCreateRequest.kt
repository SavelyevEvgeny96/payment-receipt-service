package ru.sogaz.site.paymentReceiptService.model.web.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import ru.sogaz.site.paymentReceiptService.config.ValidatorConfig.Companion.CURRENCY_MAX
import ru.sogaz.site.paymentReceiptService.model.enums.ApiVersion
import ru.sogaz.site.paymentReceiptService.model.enums.PaymentMethod
import ru.sogaz.site.paymentReceiptService.model.enums.PaymentType
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptSystem
import ru.sogaz.site.paymentReceiptService.model.enums.VatType
import ru.sogaz.site.paymentReceiptService.validation.constraint.Name
import ru.sogaz.site.paymentReceiptService.validation.constraint.PatternCurrency
import ru.sogaz.site.paymentReceiptService.validation.constraint.PatternQuantity
import ru.sogaz.site.paymentReceiptService.validation.constraint.Phone
import java.math.BigDecimal
import java.util.UUID

data class PaymentReceiptCreateRequest(
    @get:Valid
    val client: ClientInfo,
    val orderId: UUID,
    @field:NotEmpty
    @get:Valid
    val items: List<PaymentItemRequest> = emptyList(),
    @field:NotEmpty
    @get:Valid
    val payments: List<PaymentPaymentRequest> = emptyList(),
    @field:Positive
    @field:PatternCurrency
    @field:Max(value = CURRENCY_MAX)
    val total: BigDecimal,
    val depersonalization: Boolean = false,
    val system: ReceiptSystem,
    val version: ApiVersion,
)

data class ClientInfo(
    @field:NotBlank
    @field:Email
    val email: String,
    @field:Phone
    val phone: String?,
    @field:Name
    val name: String?,
)

data class PaymentItemRequest(
    @field:Size(max = 128)
    val name: String,
    @field:Positive
    @field:PatternCurrency
    @field:Max(value = CURRENCY_MAX)
    val price: BigDecimal,
    @field:Positive
    @field:PatternQuantity
    val quantity: BigDecimal,
    @field:Positive
    @field:PatternCurrency
    @field:Max(value = CURRENCY_MAX)
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
    @field:Positive
    @field:PatternCurrency
    @field:Max(value = CURRENCY_MAX)
    val sum: BigDecimal,
)
