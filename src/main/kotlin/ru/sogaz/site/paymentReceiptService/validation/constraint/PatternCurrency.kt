package ru.sogaz.site.paymentReceiptService.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.springframework.beans.factory.annotation.Qualifier
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_GETTER,
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PatternCurrencyValidator::class])
annotation class PatternCurrency(
    val message: String = "invalid currency",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class PatternCurrencyValidator(
    @param:Qualifier("currencyRegex") private val regex: Regex,
) : ConstraintValidator<PatternCurrency, Double?> {
    override fun isValid(
        value: Double?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (value == null) return true
        val stringValue = value.toBigDecimal().stripTrailingZeros().toPlainString()
        return regex.matches(stringValue)
    }
}
