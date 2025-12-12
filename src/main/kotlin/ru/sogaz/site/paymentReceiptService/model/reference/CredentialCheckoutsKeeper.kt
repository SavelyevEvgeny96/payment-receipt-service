package ru.sogaz.site.paymentReceiptService.model.reference

import ru.sogaz.site.paymentReceiptService.model.entity.CheckoutMapping

class CredentialCheckoutsKeeper(
    val checkouts: List<CheckoutMapping>,
) {
    fun findByMapping(mapping: CredentialsMapping): CheckoutMapping? =
        mapping
            .run(::mappingPredicate)
            .run(checkouts::find)

    private fun mappingPredicate(mapping: CredentialsMapping): (CheckoutMapping) -> Boolean =
        { it.product == mapping.product && it.channel == mapping.channel }
}
