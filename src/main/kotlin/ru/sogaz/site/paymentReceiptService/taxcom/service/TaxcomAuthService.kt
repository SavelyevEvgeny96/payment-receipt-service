package ru.sogaz.site.paymentReceiptService.taxcom.service

import feign.FeignException
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.config.loggerFor
import ru.sogaz.site.paymentReceiptService.taxcom.client.TaxcomClient
import ru.sogaz.site.paymentReceiptService.taxcom.config.TaxcomProperties
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomLoginRequest

@Service
@ConditionalOnProperty(prefix = "taxcom.export", name = ["enabled"], havingValue = "true")
class TaxcomAuthService(
    private val taxcomClient: TaxcomClient,
    private val taxcomProperties: TaxcomProperties,
) {
    private val log = loggerFor(javaClass)
    @Volatile
    private var sessionToken: String? = null

    fun getToken(): String = sessionToken ?: refreshToken()

    @Synchronized
    fun refreshToken(): String {
        val integratorId = requireNotBlank(taxcomProperties.api.integratorId, "taxcom.api.integrator-id")
        val login = requireNotBlank(taxcomProperties.api.login, "taxcom.api.login")
        val password = requireNotBlank(taxcomProperties.api.password, "taxcom.api.password")

        log.info("Taxcom export: запрашиваем новый sessionToken")
        val token = try {
            taxcomClient
                .login(
                    integratorId,
                    TaxcomLoginRequest(login, password),
                ).sessionToken
        } catch (ex: FeignException) {
            log.error(
                "Taxcom export: ошибка Login в Taxcom, status={}, responseBody={}",
                ex.status(),
                ex.contentUTF8(),
                ex,
            )
            throw ex
        }
        sessionToken = token
        log.info("Taxcom export: sessionToken успешно получен")
        return token
    }

    private fun requireNotBlank(value: String, propertyName: String): String =
        value.takeIf(String::isNotBlank) ?: error("Taxcom export: property $propertyName is empty")

    fun <T> executeWithAuthRetry(operationName: String, block: (String) -> T): T {
        val token = getToken()
        return try {
            block(token)
        } catch (ex: FeignException) {
            if (ex.status() == HTTP_UNAUTHORIZED) {
                log.warn("Taxcom export: получена ошибка авторизации на операции {}, обновляем токен и повторяем запрос", operationName)
                block(refreshToken())
            } else {
                throw ex
            }
        }
    }

    companion object {
        private const val HTTP_UNAUTHORIZED = 401
    }
}
