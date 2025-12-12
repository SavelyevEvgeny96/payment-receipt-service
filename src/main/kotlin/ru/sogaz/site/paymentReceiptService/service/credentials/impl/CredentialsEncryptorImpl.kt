package ru.sogaz.site.paymentReceiptService.service.credentials.impl

import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.config.loggerFor
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentReceiptService.model.entity.CheckoutMapping
import ru.sogaz.site.paymentReceiptService.model.reference.Credentials
import ru.sogaz.site.paymentReceiptService.service.credentials.CredentialsEncryptor
import kotlin.jvm.Throws

@Service
class CredentialsEncryptorImpl : CredentialsEncryptor {
    companion object {
        const val ENCRYPT_ERROR_MESSAGE = "Во время получения данных для авторизации произошла ошибка"
    }

    private val logger = loggerFor(javaClass)

    @Throws(InnerException::class)
    override fun encryptCredentials(checkoutMapping: CheckoutMapping): Credentials =
        try {
            Credentials(
                login = checkoutMapping.encryptLogin(),
                pass = checkoutMapping.encryptPassword(),
            )
        } catch (ex: Exception) {
            logger.error(ENCRYPT_ERROR_MESSAGE, ex)
            throw InnerException(getTraceId(), ENCRYPT_ERROR_MESSAGE)
        }

    private fun CheckoutMapping.encryptLogin(): String = System.getenv(login)

    private fun CheckoutMapping.encryptPassword(): String = System.getenv(password)
}
