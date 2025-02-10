package ru.sogaz.site.paymentReceiptService.service

import org.springframework.cache.CacheManager
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentDocument
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentItem
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentReceipt
import ru.sogaz.site.paymentReceiptService.model.web.request.AtolRequest
import ru.sogaz.site.paymentReceiptService.model.web.response.AtolResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.AtolStatusResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.TokenResponse
import ru.sogaz.site.paymentReceiptService.repository.reference.ConfigurationDataRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class AtolClient(
    private val restTemplate: RestTemplate,
    private val cacheManager: CacheManager,
    private val configurationDataRepository: ConfigurationDataRepository,
) {
    fun getAtolToken(): String {
        val cache = cacheManager.getCache("atolToken")
        val cachedToken = cache?.get("token", String::class.java)

        if (cachedToken != null) {
            return cachedToken
        }

        val login = configurationDataRepository.findByParamName("AtolLogin")?.paramValue
        val pass = configurationDataRepository.findByParamName("AtolPass")?.paramValue
        val url =
            configurationDataRepository.findByParamName("atolURL")?.paramValue
                ?: throw BusinessException(-1101500504)

        val response = restTemplate.getForEntity("$url/getToken?login=$login&pass=$pass", TokenResponse::class.java)

        if (!response.statusCode.is2xxSuccessful || response.body?.token == null) {
            throw BusinessException(-1101500504)
        }

        val newToken = response.body!!.token
        cache?.put("token", newToken)

        return newToken.toString()
    }

    fun sendAtolRequest(
        document: PaymentDocument,
        items: List<PaymentItem>,
        payments: List<PaymentReceipt>,
    ): String {
        val token = getAtolToken()
        val url = "${configurationDataRepository.findByParamName("atolURL")?.paramValue}/sell?token=$token"

        val request =
            AtolRequest(
                externalId = document.docId.toString(),
                service =
                    AtolRequest.AtolServiceData(
                        callbackUrl = configurationDataRepository.findByParamName("callbackURL")?.paramValue ?: "",
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
                                email = configurationDataRepository.findByParamName("companyEmail")?.paramValue ?: "",
                                inn = configurationDataRepository.findByParamName("companyInn")?.paramValue ?: "",
                                paymentAddress = configurationDataRepository.findByParamName("paymentAddress")?.paramValue ?: "",
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

    fun getPaymentStatus(document: PaymentDocument): String {
        val token = getAtolToken()

        val apiVersion = document.apiVersion.versionCode
        val groupCode =
            configurationDataRepository.findByParamName("groupCode")?.paramValue
                ?: throw BusinessException(-1101560504)

        val url = "${configurationDataRepository.findByParamName(
            "atolURL",
        )?.paramValue}/$apiVersion/$groupCode/report/${document.externalId}"

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
