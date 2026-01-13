package ru.sogaz.site.paymentReceiptService.service.receipt

import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptStatusRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptUpdateRequest

interface ReceiptStatusService {
    fun setStatus(request: PaymentReceiptStatusRequest): Receipt

    fun updateStatusFromAtol(request: PaymentReceiptUpdateRequest): Receipt

    fun updateStatusFromAtol(receipt: Receipt): Receipt
}
