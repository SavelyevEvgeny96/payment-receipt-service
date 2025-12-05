package ru.sogaz.site.paymentReceiptService.service

import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptStatusRequest
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptUpdateRequest
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptStatusResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.PaymentReceiptUpdateResponse

interface ReceiptStatusService {
    fun setStatus(request: PaymentReceiptStatusRequest): PaymentReceiptStatusResponse

    fun updateStatusFromAtol(request: PaymentReceiptUpdateRequest): PaymentReceiptUpdateResponse

    fun updateStatusFromAtol(receipt: Receipt): Receipt
}
