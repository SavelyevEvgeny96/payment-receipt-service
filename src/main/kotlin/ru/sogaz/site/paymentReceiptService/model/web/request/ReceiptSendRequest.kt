package ru.sogaz.site.paymentReceiptService.model.web.request

import ru.sogaz.site.paymentReceiptService.model.enums.ApiVersion
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptSystem
import java.util.UUID

data class ReceiptSendRequest(
    val receiptId: UUID,
    val receiptSystem: ReceiptSystem,
    val apiVersion: ApiVersion,
)
