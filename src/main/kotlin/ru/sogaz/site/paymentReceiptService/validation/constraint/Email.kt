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
@Constraint(validatedBy = [EmailValidator::class])
annotation class Email(
    val message: String = "должно иметь формат адреса электронной почты",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class EmailValidator(
    @param:Qualifier("emailRegex") private val regex: Regex,
) : ConstraintValidator<Email, String?> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (value.isNullOrBlank()) return true
        return regex.matches(value)
    }
}
