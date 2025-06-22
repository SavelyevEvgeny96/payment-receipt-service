package ru.sogaz.site.paymentReceiptService.validation

import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.ValidationException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.CODE_ERROR_VALIDATION
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.CURRENCY_VALIDATION
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.EMAIL
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.EMAIL_VALIDATION
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.INVALID_VALUE_VALIDATION
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.ITEM_NAME
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.ITEM_NAME_VALIDATION
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.ITEM_PRICE
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.ITEM_QUANTITY
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.ITEM_QUANTITY_VALIDATION
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.ITEM_SUM
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.NAME
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.NAME_VALIDATION
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.PAYMENT_METHOD
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.PAYMENT_OBJECT
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.PAYMENT_SUM
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.PAYMENT_SYSTEM
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.PAYMENT_SYSTEM_VALIDATION
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.PAYMENT_TOTAL
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.PAYMENT_TYPE
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.PAYMENT_VERSION
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.PAYMENT_VERSION_VALIDATION
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.PHONE
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.PHONE_VALIDATION
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.VAT_TYPE
import ru.sogaz.site.filterStarter.util.TraceId
import ru.sogaz.site.paymentReceiptService.loggerFor
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptCreateRequest
import ru.sogaz.site.paymentReceiptService.repository.reference.ApiVersionRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.CashRegisterRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.PaymentMethodRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.PaymentObjectRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.PaymentTypeRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.VatTypeRepository
import ru.sogaz.site.paymentReceiptService.validation.paramValidation.CurrencyValidator
import ru.sogaz.site.paymentReceiptService.validation.paramValidation.EmailValidator
import ru.sogaz.site.paymentReceiptService.validation.paramValidation.NameValidator
import ru.sogaz.site.paymentReceiptService.validation.paramValidation.PhoneValidator
import ru.sogaz.site.paymentReceiptService.validation.paramValidation.QuantityValidator
import ru.sogaz.siter.models.resonses.ValidationErrorData

class PaymentReceiptCreateRequestValidation(
    private val phoneValidator: PhoneValidator,
    private val emailValidator: EmailValidator,
    private val nameValidator: NameValidator,
    private val currencyValidator: CurrencyValidator,
    private val quantityValidator: QuantityValidator,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentObjectRepository: PaymentObjectRepository,
    private val vatTypeRepository: VatTypeRepository,
    private val paymentTypeRepository: PaymentTypeRepository,
    private val apiVersionRepository: ApiVersionRepository,
    private val cashRegisterRepository: CashRegisterRepository,
) {
    private val logger = loggerFor(javaClass)

    fun isValid(request: PaymentReceiptCreateRequest) {
        val traceId = TraceId.get()
        logger.info("Начало валидации для traceId: $traceId")

        val validationErrors = CustomPaymentReceiptErrors.Companion.validationErrors
        val listResultError = mutableListOf<ValidationErrorData?>()

        if (!emailValidator.isValid(request.client.email)) {
            listResultError.add(validationErrors[EMAIL])
            logger.warn(EMAIL_VALIDATION)
        }

        if (request.client.phone != null) {
            if (!phoneValidator.isValid(request.client.phone)) {
                listResultError.add(validationErrors[PHONE])
                logger.warn(PHONE_VALIDATION)
            }
        }

        if (request.client.name != null) {
            if (!nameValidator.isValid(request.client.name)) {
                listResultError.add(validationErrors[NAME])
                logger.warn(NAME_VALIDATION)
            }
        }

        request.items.forEachIndexed { index, paymentItemRequest ->

            if (paymentItemRequest.name.length > 128) {
                listResultError.add(validationErrors[ITEM_NAME])
                logger.warn(ITEM_NAME_VALIDATION)
            }
            if (!currencyValidator.isValid(paymentItemRequest.price)) {
                listResultError.add(validationErrors[ITEM_PRICE])
                logger.warn(CURRENCY_VALIDATION)
            }
            if (!quantityValidator.isValid(paymentItemRequest.quantity)) {
                listResultError.add(validationErrors[ITEM_QUANTITY])
                logger.warn(ITEM_QUANTITY_VALIDATION)
            }
            if (!currencyValidator.isValid(paymentItemRequest.sum)) {
                listResultError.add(validationErrors[ITEM_SUM])
                logger.warn(CURRENCY_VALIDATION)
            }

            val paymentMethod =
                paymentMethodRepository.findByPaymentMethodCode(paymentItemRequest.paymentMethod)
            if (paymentMethod == null) {
                listResultError.add(validationErrors[PAYMENT_METHOD])
                logger.warn(INVALID_VALUE_VALIDATION)
            }

            val paymentObject =
                paymentObjectRepository.findByPaymentObjectIdCode(paymentItemRequest.paymentObject)
            if (paymentObject == null) {
                listResultError.add(validationErrors[PAYMENT_OBJECT])
                logger.warn(INVALID_VALUE_VALIDATION)
            }

            val vatType =
                vatTypeRepository.findByVatTypeCode(paymentItemRequest.vat.type)
            if (vatType == null) {
                listResultError.add(validationErrors[VAT_TYPE])
                logger.warn(INVALID_VALUE_VALIDATION)
            }
        }

        request.payments.forEachIndexed { index, paymentPaymentRequest ->

            val typeCode = paymentPaymentRequest.type.toIntOrNull()
            if (typeCode == null) {
                listResultError.add(validationErrors[PAYMENT_TYPE])
                logger.warn(INVALID_VALUE_VALIDATION)
            } else {
                val paymentType = paymentTypeRepository.findByTypeIdCode(typeCode)
                if (paymentType == null) {
                    listResultError.add(validationErrors[PAYMENT_TYPE])
                    logger.warn(INVALID_VALUE_VALIDATION)
                }
            }
            if (!currencyValidator.isValid(paymentPaymentRequest.sum)) {
                listResultError.add(validationErrors[PAYMENT_SUM])
                logger.warn(CURRENCY_VALIDATION)
            }
        }

        if (!currencyValidator.isValid(request.total)) {
            listResultError.add(validationErrors[PAYMENT_TOTAL])
            logger.warn(CURRENCY_VALIDATION)
        }

        val cashRegister = cashRegisterRepository.findBySystemCode(request.system)
        if (cashRegister == null) {
            listResultError.add(validationErrors[PAYMENT_SYSTEM])
            logger.warn(PAYMENT_SYSTEM_VALIDATION)
        }

        val apiVersion = apiVersionRepository.findByVersionCode(request.version)
        if (apiVersion == null) {
            listResultError.add(validationErrors[PAYMENT_VERSION])
            logger.warn(PAYMENT_VERSION_VALIDATION)
        }

        if (listResultError.isNotEmpty()) {
            logger.error(
                "Валидация не прошла для traceId: $traceId." +
                    " Ошибки: ${listResultError.map { it?.error }}",
            )
            throw ValidationException(
                CODE_ERROR_VALIDATION,
                traceId,
                listResultError,
            )
        }

        logger.info("Валидация прошла успешно для traceId: $traceId")
    }
}
