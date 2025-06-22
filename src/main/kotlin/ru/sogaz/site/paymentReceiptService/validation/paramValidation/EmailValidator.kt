package ru.sogaz.site.paymentReceiptService.validation.paramValidation

class EmailValidator(
    private val emailRegex: Regex,
) {
    fun isValid(value: String?): Boolean = value?.matches(emailRegex) ?: false
}
