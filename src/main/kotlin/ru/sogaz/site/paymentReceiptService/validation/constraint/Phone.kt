package ru.sogaz.site.paymentReceiptService.validation.constraint

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.springframework.beans.factory.annotation.Qualifier
import kotlin.reflect.KClass
import kotlin.text.isNullOrBlank

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_GETTER,
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PhoneValidator::class])
annotation class Phone(
    val message: String = "должно иметь формат номера телефона",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class PhoneValidator(
    @param:Qualifier("phoneRegex") private val regex: Regex,
) : ConstraintValidator<Phone, String?> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (value.isNullOrBlank()) return true
        return regex.matches(value)
    }
}
