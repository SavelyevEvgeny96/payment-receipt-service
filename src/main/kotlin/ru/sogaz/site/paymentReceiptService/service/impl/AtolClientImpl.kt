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
import ru.sogaz.site.filterStarter.util.TraceId
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
    private val objectsMapper: ObjectMapper,
    private val configurationDataProperties: ConfigurationDataProperties,
) : AtolClient {
    private val log = loggerFor(javaClass)

    companion object {
        private const val ATOL_TOKEN = "atolToken"
        private const val TOKEN = "token"
        private const val PAYMENT_METHOD = "Payment Method not found in items"
        private const val PAYMENT_OBJECT = "Payment Object not found in items"
        private const val VAT_TYPE = "Vat Type not found in items"
        private const val PAYMENT_TYPE = "Payment Type not found in payments"
    }

    override fun getAtolToken(): String {
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

        val response =
            restTemplate.postForEntity(
                "$url/getToken",
                entity,
                TokenResponse::class.java,
            )

        if (!response.statusCode.is2xxSuccessful || response.body?.token == null) {
            throw BusinessException(CODE_ERROR_UNAUTHORIZED, TraceId.get())
        }

        val newToken = response.body?.token ?: throw BusinessException(CODE_ERROR_UNAUTHORIZED, TraceId.get())
        cache?.put(TOKEN, newToken)

        return newToken
    }

    override fun sendAtolRequest(
        document: PaymentDocument,
        items: List<PaymentItem>,
        payments: List<PaymentReceipt>,
    ): String {
        try {
            val token = getAtolToken()
            val url =
                "${configurationDataProperties.atolURL}/${configurationDataProperties.groupCode}/sell?token=$token"

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
                                                ?: throw InnerException(TraceId.get(), PAYMENT_METHOD),
                                        paymentObject =
                                            item.paymentObject?.paymentObjectIdCode
                                                ?: throw InnerException(TraceId.get(), PAYMENT_OBJECT),
                                        vat =
                                            AtolRequest.AtolVatData(
                                                type =
                                                    item.vatType?.vatTypeCode
                                                        ?: throw InnerException(TraceId.get(), VAT_TYPE),
                                            ),
                                    )
                                },
                            payments =
                                payments.map { payment ->
                                    AtolRequest.AtolPaymentData(
                                        sum = payment.sum,
                                        type =
                                            payment.paymentType?.typeIdCode ?: throw InnerException(
                                                TraceId.get(),
                                                PAYMENT_TYPE,
                                            ),
                                    )
                                },
                            total = document.total,
                        ),
                    timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
                )

            val requestJson = objectsMapper.writeValueAsString(request)
            log.warn("Request JSON to Atol: $requestJson")

            val headers =
                HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
                }
            val entity = HttpEntity(request, headers)

            val rawResponse = restTemplate.exchange(url, HttpMethod.POST, entity, String::class.java)
            log.warn("Response JSON from Atol: ${rawResponse.body}")

            val responseObject = objectsMapper.readValue(rawResponse.body!!, AtolResponse::class.java)

            if (responseObject.uuid.isEmpty()) {
                throw BusinessException(CODE_ERROR_PAYMENT_SYSTEM_NOT_FOUND, TraceId.get())
            }

            return responseObject.uuid
        } catch (e: BusinessException) {
            log.warn(e.message)
            throw e
        } catch (e: Exception) {
            log.warn(e.message)
            throw InnerException(TraceId.get(), e.message)
        }
    }

    override fun getPaymentStatus(document: PaymentDocument): String {
        try {
            val token = getAtolToken()

//        val apiVersion = document.apiVersion?.versionCode
            val groupCode = configurationDataProperties.groupCode

            val url = "${configurationDataProperties.atolURL}/$groupCode/report/${document.externalId}"

            val headers =
                HttpHeaders().apply {
                    set("Token", token)
                    contentType = MediaType.APPLICATION_JSON
                }

            val entity = HttpEntity<Unit>(headers)

            val response = restTemplate.exchange(url, HttpMethod.GET, entity, String::class.java)
            log.warn("Response JSON from Atol: ${response.body}")

            val responseObject = objectsMapper.readValue(response.body, AtolStatusResponse::class.java)

//        if (!response.statusCode.is2xxSuccessful || response.body?.status == null) {
//            throw BusinessException(CODE_ERROR_UPDATE_STATUS_SYSTEM_NOT_FOUND, TraceId.get())
//        }

            return responseObject.status
        } catch (e: BusinessException) {
            log.warn(e.message)
            throw e
        } catch (e: Exception) {
            log.warn(e.message)
            throw InnerException(TraceId.get(), e.message)
        }
    }
}
