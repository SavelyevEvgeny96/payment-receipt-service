package ru.sogaz.site.paymentReceiptService.validation.paramValidation

class QuantityValidator(
    private val quantityRegex: Regex,
) {
    fun isValid(value: Double?): Boolean {
        if (value == null) return false

        // Приводим к строке с максимум 3 знаками после точки (без лишних нулей)
        val stringValue = value.toBigDecimal().stripTrailingZeros().toPlainString()
        return stringValue.matches(quantityRegex)
    }
}
