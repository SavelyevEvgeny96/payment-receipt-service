package ru.sogaz.site.paymentReceiptService.service.impl

import org.springframework.cache.CacheManager
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
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
) : AtolClient {
    override fun getAtolToken(): String {
        val cache = cacheManager.getCache("atolToken")
        val cachedToken = cache?.get("token", String::class.java)

        if (cachedToken != null) {
            return cachedToken
        }

        val login = configurationDataProperties.AtolLogin
        val pass = configurationDataProperties.AtolPass
        val url = configurationDataProperties.atolURL

        val response = restTemplate.getForEntity("$url/getToken?login=$login&pass=$pass", TokenResponse::class.java)

        if (!response.statusCode.is2xxSuccessful || response.body?.token == null) {
            throw BusinessException(-1101500504)
        }

        val newToken = response.body!!.token
        cache?.put("token", newToken)

        return newToken.toString()
    }

    override fun sendAtolRequest(
        document: PaymentDocument,
        items: List<PaymentItem>,
        payments: List<PaymentReceipt>,
    ): String {
        val token = getAtolToken()
        val url = "${configurationDataProperties.atolURL}/sell?token=$token"

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
                                    paymentMethod = item.paymentMethod.paymentMethodCode,
                                    paymentObject = item.paymentObject.paymentObjectIdCode,
                                    vat = AtolRequest.AtolVatData(type = item.vatType.vatTypeCode),
                                )
                            },
                        payments =
                            payments.map { payment ->
                                AtolRequest.AtolPaymentData(
                                    sum = payment.sum,
                                    type = payment.paymentType.typeIdCode,
                                )
                            },
                        total = document.total,
                    ),
                timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
            )

        val response = restTemplate.postForEntity(url, request, AtolResponse::class.java)

        if (!response.statusCode.is2xxSuccessful || response.body?.externalId == null) {
            throw BusinessException(-1101500504)
        }

        return response.body!!.externalId
    }

    override fun getPaymentStatus(document: PaymentDocument): String {
        val token = getAtolToken()

        val apiVersion = document.apiVersion.versionCode
        val groupCode = configurationDataProperties.groupCode

        val url = "${configurationDataProperties.atolURL}/$apiVersion/$groupCode/report/${document.externalId}"

        val headers =
            HttpHeaders().apply {
                set("Token", token) // Передаём токен
                contentType = MediaType.APPLICATION_JSON
            }

        val entity = HttpEntity<Unit>(headers)

        val response = restTemplate.exchange(url, HttpMethod.GET, entity, AtolStatusResponse::class.java)

        if (!response.statusCode.is2xxSuccessful || response.body?.status == null) {
            throw BusinessException(-1101560504)
        }

        return response.body!!.status
    }
}
