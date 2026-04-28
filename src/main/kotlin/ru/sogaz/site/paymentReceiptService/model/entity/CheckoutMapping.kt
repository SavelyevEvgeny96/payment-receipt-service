package ru.sogaz.site.paymentReceiptService.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "checkout_mappings")
data class CheckoutMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    var product: String? = null,
    var channel: String? = null,
    var login: String? = null,
    var password: String? = null,
    var groupCode: String? = null,
)
