package ru.sogaz.site.paymentReceiptService.mapper

import org.mapstruct.AfterMapping
import org.mapstruct.MappingTarget
import org.mapstruct.Named
import org.springframework.stereotype.Component
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.util.TraceId
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentDocument
import ru.sogaz.site.paymentReceiptService.model.reference.ApiVersion
import ru.sogaz.site.paymentReceiptService.model.reference.CashRegister
import ru.sogaz.site.paymentReceiptService.model.reference.PaymentMethod
import ru.sogaz.site.paymentReceiptService.model.reference.PaymentObject
import ru.sogaz.site.paymentReceiptService.model.reference.PaymentType
import ru.sogaz.site.paymentReceiptService.model.reference.VatType
import ru.sogaz.site.paymentReceiptService.repository.reference.ApiVersionRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.CashRegisterRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.CheckStatusRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.PaymentMethodRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.PaymentObjectRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.PaymentTypeRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.VatTypeRepository

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
    companion object {
        const val NOT_FOUND = "Reference data not found"
    }

    @Named("mapSystem")
    fun mapSystem(system: String): CashRegister =
        cashRegisterRepository.findBySystemCode(system) ?: throw InnerException(TraceId.get(), NOT_FOUND)

    @Named("mapVersion")
    fun mapVersion(version: String): ApiVersion =
        apiVersionRepository.findByVersionCode(version) ?: throw InnerException(TraceId.get(), NOT_FOUND)

    @Named("mapPaymentMethod")
    fun mapPaymentMethod(code: String): PaymentMethod =
        paymentMethodRepository.findByPaymentMethodCode(code) ?: throw InnerException(TraceId.get(), NOT_FOUND)

    @Named("mapPaymentObject")
    fun mapPaymentObject(code: String): PaymentObject =
        paymentObjectRepository.findByPaymentObjectIdCode(code) ?: throw InnerException(TraceId.get(), NOT_FOUND)

    @Named("mapVatType")
    fun mapVatType(code: String): VatType = vatTypeRepository.findByVatTypeCode(code) ?: throw InnerException(TraceId.get(), NOT_FOUND)

    @Named("mapPaymentType")
    fun mapPaymentType(code: String): PaymentType =
        paymentTypeRepository.findByTypeIdCode(code.toInt()) ?: throw InnerException(TraceId.get(), NOT_FOUND)

    @AfterMapping
    fun setInitialStatus(
        @MappingTarget document: PaymentDocument,
    ) {
        document.status = checkStatusRepository.findByStateId("new") ?: throw InnerException(TraceId.get(), NOT_FOUND)
    }
}
