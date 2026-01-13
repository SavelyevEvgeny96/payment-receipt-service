package ru.sogaz.site.paymentReceiptService.service.credentials.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentReceiptService.dao.CheckoutMappingDao
import ru.sogaz.site.paymentReceiptService.mapper.credentials.CredentialsMapper
import ru.sogaz.site.paymentReceiptService.model.credential.CredentialCheckoutsKeeper
import ru.sogaz.site.paymentReceiptService.model.credential.Credentials
import ru.sogaz.site.paymentReceiptService.model.credential.CredentialsMapping
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.orThrow
import ru.sogaz.site.paymentReceiptService.service.credentials.CredentialsEncryptor
import ru.sogaz.site.paymentReceiptService.service.credentials.CredentialsManager

@Service
class CredentialsManagerImpl(
    private val checkoutMappingDao: CheckoutMappingDao,
    private val credentialsEncryptor: CredentialsEncryptor,
    private val credentialsMapper: CredentialsMapper,
) : CredentialsManager {
    companion object {
        private const val DEFAULT_ALL = "ALL"
        const val NOT_FOUND_CREDENTIALS_ERROR_MESSAGE = "Не удалось найти данные для авторизации в кассе для продукта: {} и канала: {}"
    }

    override fun findCredentials(receipt: Receipt): Credentials = findCredentials(receipt.product, receipt.channel)

    override fun findCredentials(
        product: String?,
        channel: String?,
    ): Credentials {
        val credentialCheckoutsKeeper =
            checkoutMappingDao
                .findAll()
                .run(::CredentialCheckoutsKeeper)

        return makeMappings(product, channel)
            .firstNotNullOfOrNull(credentialCheckoutsKeeper::findByMapping)
            .orThrow { generateNotFoundError(product, channel) }
            .run(credentialsEncryptor::encryptCredentials)
    }

    private fun makeMappings(
        product: String?,
        channel: String?,
    ): List<CredentialsMapping> =
        generateCombinations(product, channel)
            .run(::makeMappings)

    private fun makeMappings(combinations: List<Pair<String?, String?>>): List<CredentialsMapping> =
        combinations.map(credentialsMapper::toCredentialsMapping)

    private fun generateCombinations(
        product: String?,
        channel: String?,
    ): List<Pair<String?, String?>> =
        listOf(
            product to channel,
            product to DEFAULT_ALL,
            DEFAULT_ALL to channel,
            DEFAULT_ALL to DEFAULT_ALL,
        )

    private fun generateNotFoundError(
        product: String?,
        channel: String?,
    ): InnerException = InnerException(getTraceId(), NOT_FOUND_CREDENTIALS_ERROR_MESSAGE.format(product, channel))
}
