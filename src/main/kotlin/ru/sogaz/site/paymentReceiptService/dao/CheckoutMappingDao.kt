package ru.sogaz.site.paymentReceiptService.dao

import ru.sogaz.site.paymentReceiptService.model.entity.CheckoutMapping

interface CheckoutMappingDao {
    fun findAll(): List<CheckoutMapping>
}
