package ru.sogaz.site.paymentReceiptService.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.springframework.beans.factory.annotation.Qualifier
import java.math.BigDecimal
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_GETTER,
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PatternCurrencyValidator::class])
annotation class PatternCurrency(
    val message: String = "должно быть не больше 3 знаков после запятой(точки)",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class PatternCurrencyValidator(
    @param:Qualifier("currencyRegex") private val regex: Regex,
) : ConstraintValidator<PatternCurrency, BigDecimal?> {
    override fun isValid(
        value: BigDecimal?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (value == null) return true
        val stringValue = value.stripTrailingZeros().toPlainString()
        return regex.matches(stringValue)
    }
}
