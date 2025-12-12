package ru.sogaz.site.paymentReceiptService.service.atol.impl

import jakarta.validation.ConstraintViolationException
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import ru.sogaz.site.exceptionStarter.starter.config.loggerFor
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.filterStarter.services.RequestInfo.getTraceId
import ru.sogaz.site.paymentReceiptService.clients.AtolClient
import ru.sogaz.site.paymentReceiptService.config.CacheConfig
import ru.sogaz.site.paymentReceiptService.model.atol.request.AtolTokenRequest
import ru.sogaz.site.paymentReceiptService.service.atol.AtolAuthService

@Service
class AtolAuthServiceImpl(
    private val atolClient: AtolClient,
) : AtolAuthService {
    companion object {
        private const val INVALID_TOKEN_ERROR_MESSAGE = "ATOL вернул невалидный токен: {}"
    }

    private val logger = loggerFor(javaClass)

    @Cacheable(CacheConfig.ATOL_TOKEN_CACHE)
    override fun getToken(atolTokenRequest: AtolTokenRequest): String =
        try {
            atolTokenRequest
                .run(atolClient::getToken)
                .token
        } catch (ex: Exception) {
            if (ex is ConstraintViolationException) {
                logger.error(INVALID_TOKEN_ERROR_MESSAGE, ex.message)
            }
            throw InnerException(getTraceId(), ex.message)
        }
}
