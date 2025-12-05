package ru.sogaz.site.paymentReceiptService.model.web.request.atol

import com.fasterxml.jackson.annotation.JsonProperty
import ru.sogaz.site.paymentReceiptService.model.reference.ServiceData

data class AtolRequest(
    @param:JsonProperty("external_id")
    val externalId: String,
    val service: ServiceData,
    val receipt: AtolReceiptData,
    var timestamp: String?,
)
