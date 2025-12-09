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
@Constraint(validatedBy = [PatternQuantityValidator::class])
annotation class PatternQuantity(
    val message: String = "должно быть не больше 2 знаков после запятой(точки)",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class PatternQuantityValidator(
    @param:Qualifier("quantityRegex") private val regex: Regex,
) : ConstraintValidator<PatternQuantity, BigDecimal?> {
    override fun isValid(
        value: BigDecimal?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (value == null) return true
        val stringValue = value.stripTrailingZeros().toPlainString()
        return regex.matches(stringValue)
    }
}
