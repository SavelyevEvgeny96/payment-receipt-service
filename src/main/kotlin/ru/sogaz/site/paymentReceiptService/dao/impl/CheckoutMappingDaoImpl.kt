package ru.sogaz.site.paymentReceiptService.dao.impl

import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentReceiptService.dao.CheckoutMappingDao
import ru.sogaz.site.paymentReceiptService.model.entity.CheckoutMapping
import ru.sogaz.site.paymentReceiptService.repository.CheckoutMappingRepository

@Repository
class CheckoutMappingDaoImpl(
    private val checkoutMappingRepository: CheckoutMappingRepository,
) : CheckoutMappingDao {
    override fun findAll(): List<CheckoutMapping> = checkoutMappingRepository.findAll()
}
