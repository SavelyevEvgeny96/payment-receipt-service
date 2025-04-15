package ru.sogaz.site.paymentReceiptService.model.web.request

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class PaymentReceiptCreateRequest(
    @field:Valid val client: ClientInfo,
    val userId: String?,
    @field:NotEmpty val items: List<PaymentItemRequest>,
    @field:NotEmpty val payments: List<PaymentPaymentRequest>,
    @field:NotNull val total: Double,
    @field:NotBlank @field:Pattern(regexp = "Atol") val system: String,
    @field:NotBlank @field:Pattern(regexp = "v4") val version: String,
) {
    data class ClientInfo(
        @field:Email val email: String,
        @field:Pattern(regexp = "[+0-9()\\- ]{1,64}") val phone: String?,
        @field:Pattern(regexp = "[А-Яа-я\\- ]{2,256}") val name: String?,
    )

    data class PaymentItemRequest(
        @field:Size(max = 128) val name: String,
        @field:DecimalMin("0.01") val price: Double,
        @field:DecimalMin("0.001") val quantity: Double,
        @field:DecimalMin("0.01") val sum: Double,
        @field:NotBlank val paymentMethod: String,
        @field:NotBlank val paymentObject: String,
        @field:Valid val vat: VatRequest,
    )

    data class VatRequest(
        @field:NotBlank val type: String,
    )

    data class PaymentPaymentRequest(
        @field:NotBlank val type: String,
        @field:DecimalMin("0.01") val sum: Double,
    )
}
