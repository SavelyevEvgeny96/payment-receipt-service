package ru.sogaz.site.paymentReceiptService.validation.paramValidation

class PhoneValidator(
    private val codeRegex: Regex,
) {
    fun isValid(value: String?): Boolean = value?.matches(codeRegex) ?: false
}
