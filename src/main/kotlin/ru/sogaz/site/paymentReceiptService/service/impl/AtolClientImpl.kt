package ru.sogaz.site.paymentReceiptService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.cache.CacheManager
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.CODE_ERROR_PAYMENT_SYSTEM_NOT_FOUND
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.CODE_ERROR_UNAUTHORIZED
import ru.sogaz.site.exceptionStarter.starter.service.impl.CustomPaymentReceiptErrors.Companion.CODE_ERROR_UPDATE_STATUS_SYSTEM_NOT_FOUND
import ru.sogaz.site.filterStarter.services.RequestInfo
import ru.sogaz.site.paymentReceiptService.loggerFor
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentDocument
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentItem
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentReceipt
import ru.sogaz.site.paymentReceiptService.model.web.request.AtolRequest
import ru.sogaz.site.paymentReceiptService.model.web.response.AtolResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.AtolStatusResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.TokenResponse
import ru.sogaz.site.paymentReceiptService.properties.ConfigurationDataProperties
import ru.sogaz.site.paymentReceiptService.service.AtolClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AtolClientImpl(
    private val restTemplate: RestTemplate,
    private val cacheManager: CacheManager,
    private val configurationDataProperties: ConfigurationDataProperties,
    private val objectMapper: ObjectMapper,
) : AtolClient {
    private val log = loggerFor(javaClass)

    companion object {
        private const val ATOL_TOKEN = "atolToken"
        private const val TOKEN = "token"
        private const val GET_TOKEN = "getToken"
        private const val SELL_TOKEN = "sell?token="
        private const val REPORT = "report"

        private const val PAYMENT_METHOD = "Payment Method not found in items"
        private const val PAYMENT_OBJECT = "Payment Object not found in items"
        private const val VAT_TYPE = "Vat Type not found in items"
        private const val PAYMENT_TYPE = "Payment Type not found in payments"
    }

    override fun getAtolToken(apiVersion: String): String {
        try {
            val cache = cacheManager.getCache(ATOL_TOKEN)
            val cachedToken = cache?.get(TOKEN, String::class.java)

            if (cachedToken != null) {
                return cachedToken
            }

            val login = configurationDataProperties.atolLogin
            val pass = configurationDataProperties.atolPass
            val url = configurationDataProperties.atolURL

            val requestBody =
                mapOf(
                    "login" to login,
                    "pass" to pass,
                )

            val headers =
                HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
                }

            val entity = HttpEntity(requestBody, headers)
            val atolUrl = "$url/$apiVersion/$GET_TOKEN"

            log.info("TOKEN_REQUEST_INFO:\nEntity:\n{}\nAtolUrl:\n{}", entity.toJson(), atolUrl.toJson())

            log.info(objectMapper.writeValueAsString(entity))
            log.info(objectMapper.writeValueAsString(atolUrl))

            val response =
                restTemplate.postForEntity(
                    atolUrl,
                    entity,
                    TokenResponse::class.java,
                )

            log.info("TOKEN_RESPONSE_INFO:\n{}", response.toJson())

            val newToken = response.body!!.token ?: throw BusinessException(CODE_ERROR_UNAUTHORIZED, RequestInfo.getTraceId())
            cache?.put(TOKEN, newToken)

            return newToken
        } catch (e: Exception) {
            log.error(e, e.message)
            throw BusinessException(CODE_ERROR_UNAUTHORIZED, RequestInfo.getTraceId())
        }
    }

    override fun sendAtolRequest(
        document: PaymentDocument,
        items: List<PaymentItem>,
        payments: List<PaymentReceipt>,
        apiVersion: String,
    ): String {
        val traceId = RequestInfo.getTraceId()
        try {
            val token = getAtolToken(apiVersion)

            val atolURL = configurationDataProperties.atolURL
            val groupCode = configurationDataProperties.groupCode

            val url = "$atolURL/$apiVersion/$groupCode/$SELL_TOKEN$token"

            val request =
                AtolRequest(
                    externalId = document.docId.toString(),
                    service =
                        AtolRequest.AtolServiceData(
                            callbackUrl = configurationDataProperties.callbackURL,
                        ),
                    receipt =
                        AtolRequest.AtolReceiptData(
                            client =
                                AtolRequest.AtolClientData(
                                    email = document.clientEmail,
                                    phone = document.clientPhone,
                                ),
                            company =
                                AtolRequest.AtolCompanyData(
                                    email = configurationDataProperties.companyEmail,
                                    inn = configurationDataProperties.companyInn,
                                    paymentAddress = configurationDataProperties.paymentAddress,
                                ),
                            items =
                                items.map { item ->
                                    AtolRequest.AtolItemData(
                                        name = item.name,
                                        price = item.price,
                                        quantity = item.quantity,
                                        sum = item.sum,
                                        paymentMethod =
                                            item.paymentMethod?.paymentMethodCode
                                                ?: throw InnerException(traceId, PAYMENT_METHOD),
                                        paymentObject =
                                            item.paymentObject?.paymentObjectIdCode
                                                ?: throw InnerException(traceId, PAYMENT_OBJECT),
                                        vat =
                                            AtolRequest.AtolVatData(
                                                type =
                                                    item.vatType?.vatTypeCode
                                                        ?: throw InnerException(traceId, VAT_TYPE),
                                            ),
                                    )
                                },
                            payments =
                                payments.map { payment ->
                                    AtolRequest.AtolPaymentData(
                                        sum = payment.sum,
                                        type =
                                            payment.paymentType?.typeIdCode ?: throw InnerException(
                                                traceId,
                                                PAYMENT_TYPE,
                                            ),
                                    )
                                },
                            total = document.total,
                        ),
                    timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
                )

            log.warn(objectMapper.writeValueAsString(request))

            val headers =
                HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
                }
            val entity = HttpEntity(request, headers)

            val response = restTemplate.postForEntity(url, entity, AtolResponse::class.java)

            log.warn(objectMapper.writeValueAsString(response))

            return response.body!!.uuid
        } catch (e: BusinessException) {
            log.warn(e.message)
            throw e
        } catch (e: Exception) {
            log.warn(e.message)
            throw BusinessException(CODE_ERROR_PAYMENT_SYSTEM_NOT_FOUND, traceId)
        }
    }

    override fun getPaymentStatus(
        externalId: String,
        apiVersion: String,
    ): String {
        val traceId = RequestInfo.getTraceId()
        try {
            val atolURL = configurationDataProperties.atolURL
            val groupCode = configurationDataProperties.groupCode

            val url = "$atolURL/$apiVersion/$groupCode/$REPORT/$externalId"

            val token = getAtolToken(apiVersion)

            val headers =
                HttpHeaders().apply {
                    set("Token", token)
                    contentType = MediaType.APPLICATION_JSON
                }

            val entity = HttpEntity<Unit>(headers)

            val response = restTemplate.exchange(url, HttpMethod.GET, entity, AtolStatusResponse::class.java)

            return response.body!!.status
        } catch (e: BusinessException) {
            log.warn(e.message)
            throw e
        } catch (e: Exception) {
            log.warn(e.message)
            throw BusinessException(CODE_ERROR_UPDATE_STATUS_SYSTEM_NOT_FOUND, traceId)
        }
    }

    private fun Any.toJson(): String = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
}
