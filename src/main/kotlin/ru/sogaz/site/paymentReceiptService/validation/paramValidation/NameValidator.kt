package ru.sogaz.site.paymentReceiptService.validation.paramValidation

class NameValidator(
    private val nameRegex: Regex,
) {
    fun isValid(value: String?): Boolean = value?.matches(nameRegex) ?: false
}
