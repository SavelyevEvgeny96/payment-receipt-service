package ru.sogaz.site.paymentReceiptService.model.exception

import java.lang.RuntimeException

class SendReceiptException(
    message: String,
) : RuntimeException(message)
