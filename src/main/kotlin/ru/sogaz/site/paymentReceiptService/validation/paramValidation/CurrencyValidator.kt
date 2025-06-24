package ru.sogaz.site.paymentReceiptService.validation.paramValidation

class CurrencyValidator(
    private val priceRegex: Regex,
    private val maxValue: Double,
) {
    fun isValid(value: Double?): Boolean {
        if (value == null) return false
        if (value > maxValue) return false

        val stringValue = value.toBigDecimal().stripTrailingZeros().toPlainString()
        return stringValue.matches(priceRegex)
    }
}
