package ru.sogaz.site.paymentReceiptService.model.exception

import java.lang.RuntimeException

class CreateReceiptException(
    cause: Throwable,
) : RuntimeException(cause)
