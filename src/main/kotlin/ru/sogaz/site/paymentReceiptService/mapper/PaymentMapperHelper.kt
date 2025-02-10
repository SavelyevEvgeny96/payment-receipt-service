package ru.sogaz.site.paymentReceiptService.mapper

import org.mapstruct.Named
import org.springframework.stereotype.Component
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.paymentReceiptService.model.reference.*
import ru.sogaz.site.paymentReceiptService.repository.reference.*

@Component
class PaymentMapperHelper(
    private val cashRegisterRepository: CashRegisterRepository,
    private val apiVersionRepository: ApiVersionRepository,
    private val checkStatusRepository: CheckStatusRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentObjectRepository: PaymentObjectRepository,
    private val vatTypeRepository: VatTypeRepository,
    private val paymentTypeRepository: PaymentTypeRepository,
) {
    @Named("mapSystem")
    fun mapSystem(system: String): CashRegister = cashRegisterRepository.findBySystemCode(system) ?: throw BusinessException(-1101550422)

    @Named("mapVersion")
    fun mapVersion(version: String): ApiVersion = apiVersionRepository.findByVersionCode(version) ?: throw BusinessException(-1101550422)

    @Named("mapPaymentMethod")
    fun mapPaymentMethod(code: String): PaymentMethod =
        paymentMethodRepository.findByPaymentMethodCode(code) ?: throw BusinessException(-1101550422)

    @Named("mapPaymentObject")
    fun mapPaymentObject(code: String): PaymentObject =
        paymentObjectRepository.findByPaymentObjectIdCode(code) ?: throw BusinessException(-1101550422)

    @Named("mapVatType")
    fun mapVatType(code: String): VatType = vatTypeRepository.findByVatTypeCode(code) ?: throw BusinessException(-1101550422)

    @Named("mapPaymentType")
    fun mapPaymentType(code: String): PaymentType =
        paymentTypeRepository.findByTypeIdCode(code.toInt()) ?: throw BusinessException(-1101550422)

    fun getInitialStatus(): CheckStatus = checkStatusRepository.findByStateId("new") ?: throw BusinessException(-1101550422)
}
