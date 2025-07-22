package ru.sogaz.site.paymentReceiptService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentDocument
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentItem
import ru.sogaz.site.paymentReceiptService.model.entity.PaymentReceipt
import ru.sogaz.site.paymentReceiptService.model.reference.ApiVersion
import ru.sogaz.site.paymentReceiptService.model.reference.PaymentMethod
import ru.sogaz.site.paymentReceiptService.model.reference.PaymentObject
import ru.sogaz.site.paymentReceiptService.model.reference.PaymentType
import ru.sogaz.site.paymentReceiptService.model.reference.VatType
import ru.sogaz.site.paymentReceiptService.model.web.response.AtolResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.AtolStatusResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.ErrorInfo
import ru.sogaz.site.paymentReceiptService.model.web.response.TokenResponse
import ru.sogaz.site.paymentReceiptService.properties.ConfigurationDataProperties
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class AtolClientImplTest {
    @Mock
    lateinit var restTemplate: RestTemplate

    @Mock
    lateinit var cacheManager: CacheManager

    @Mock
    lateinit var cache: Cache

    @Mock
    lateinit var objectsMapper: ObjectMapper

    @Mock
    lateinit var config: ConfigurationDataProperties

    @InjectMocks
    lateinit var atolClient: AtolClientImpl

    @Test
    fun `токен возвращается из кэша`() {
        `when`(cacheManager.getCache("atolToken")).thenReturn(cache)
        `when`(cache.get("token", String::class.java)).thenReturn("cached-token")

        val token = atolClient.getAtolToken()

        assertEquals("cached-token", token)
    }

    @Test
    fun `токен запрашивается и кэшируется, если не найден`() {
        `when`(cacheManager.getCache("atolToken")).thenReturn(cache)
        `when`(cache.get("token", String::class.java)).thenReturn(null)

        `when`(config.atolLogin).thenReturn("user")
        `when`(config.atolPass).thenReturn("pass")
        `when`(config.atolURL).thenReturn("http://atol")

        val requestBody =
            mapOf(
                "login" to config.atolLogin,
                "pass" to config.atolPass,
            )

        val header =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val entityToken = HttpEntity(requestBody, header)

        val response =
            ResponseEntity.ok(TokenResponse("new-token", ErrorInfo("1", 1, "some_error", "error"), "20-10-2025"))
        `when`(
            restTemplate.postForEntity("http://atol/getToken", entityToken, TokenResponse::class.java),
        ).thenReturn(response)

        val token = atolClient.getAtolToken()

        assertEquals("new-token", token)
        verify(cache).put("token", "new-token")
    }

    @Test
    fun `если ответ от токена плохой — кидается BusinessException`() {
        `when`(cacheManager.getCache("atolToken")).thenReturn(cache)
        `when`(cache.get("token", String::class.java)).thenReturn(null)

        `when`(config.atolLogin).thenReturn("login")
        `when`(config.atolPass).thenReturn("pass")
        `when`(config.atolURL).thenReturn("http://atol")

        val requestBody =
            mapOf(
                "login" to config.atolLogin,
                "pass" to config.atolPass,
            )

        val header =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val entityToken = HttpEntity(requestBody, header)

        val badResponse = ResponseEntity<TokenResponse>(null, HttpStatus.BAD_REQUEST)
        `when`(restTemplate.postForEntity("http://atol/getToken", entityToken, TokenResponse::class.java))
            .thenReturn(badResponse)

        assertThrows(BusinessException::class.java) {
            atolClient.getAtolToken()
        }
    }

    @Test
    fun `успешная отправка чека возвращает externalId`() {
        val doc = mock(PaymentDocument::class.java)
        val item = mock(PaymentItem::class.java)
        val payment = mock(PaymentReceipt::class.java)

        `when`(doc.docId).thenReturn(UUID.randomUUID())
        `when`(doc.clientEmail).thenReturn("test@mail.com")
        `when`(doc.clientPhone).thenReturn("123456")
        `when`(doc.total).thenReturn(100.0)

        `when`(config.atolURL).thenReturn("http://atol")
        `when`(config.callbackURL).thenReturn("http://cb")
        `when`(config.companyEmail).thenReturn("c@mail.com")
        `when`(config.companyInn).thenReturn("1234567890")
        `when`(config.paymentAddress).thenReturn("Address")

        val paymentMethod = mock(PaymentMethod::class.java)
        `when`(paymentMethod.paymentMethodCode).thenReturn("code")

        val paymentObject = mock(PaymentObject::class.java)
        `when`(paymentObject.paymentObjectIdCode).thenReturn("obj")

        val vatType = mock(VatType::class.java)
        `when`(vatType.vatTypeCode).thenReturn("vat")

        `when`(item.name).thenReturn("Item")
        `when`(item.price).thenReturn(10.0)
        `when`(item.quantity).thenReturn(1.0)
        `when`(item.sum).thenReturn(10.0)
        `when`(item.paymentMethod).thenReturn(paymentMethod)
        `when`(item.paymentObject).thenReturn(paymentObject)
        `when`(item.vatType).thenReturn(vatType)

        val paymentType = mock(PaymentType::class.java)
        `when`(paymentType.typeIdCode).thenReturn(1)
        `when`(payment.sum).thenReturn(10.0)
        `when`(payment.paymentType).thenReturn(paymentType)

        `when`(cacheManager.getCache("atolToken")).thenReturn(cache)
        `when`(cache.get("token", String::class.java)).thenReturn(null)
        `when`(config.atolLogin).thenReturn("login")
        `when`(config.atolPass).thenReturn("pass")

        val requestBody =
            mapOf(
                "login" to config.atolLogin,
                "pass" to config.atolPass,
            )

        val header =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val entityToken = HttpEntity(requestBody, header)

        `when`(restTemplate.postForEntity("http://atol/getToken", entityToken, TokenResponse::class.java))
            .thenReturn(ResponseEntity.ok(TokenResponse("token123", ErrorInfo("1", 1, "some_error", "error"), "")))

        val atolResponse = AtolResponse("external-123", "wait")
        `when`(restTemplate.postForEntity(anyString(), any(), eq(AtolResponse::class.java)))
            .thenReturn(ResponseEntity.ok(atolResponse))

        val result = atolClient.sendAtolRequest(doc, listOf(item), listOf(payment))
        assertEquals("external-123", result)

// //        val captor = ArgumentCaptor.forClass(AtolRequest::class.java)
// //        verify(restTemplate).postForEntity(
// //            eq("http://atol/sell?token=token123"),
// //            captor.capture(),
// //            eq(AtolResponse::class.java),
// //        )
//
//        val sentRequest = captor.value
//        assertEquals("test@mail.com", sentRequest.receipt.client.email)
//        assertEquals("123456", sentRequest.receipt.client.phone)
//        assertEquals(100.0, sentRequest.receipt.total)
//        assertEquals(1, sentRequest.receipt.items.size)
//        assertEquals("Item", sentRequest.receipt.items[0].name)
    }

    @Test
    fun `getPaymentStatus возвращает статус`() {
        val doc = mock(PaymentDocument::class.java)
        val apiVersion = mock(ApiVersion::class.java)

        `when`(doc.externalId).thenReturn("ext-1")
        `when`(doc.apiVersion).thenReturn(apiVersion)
        `when`(apiVersion.versionCode).thenReturn("v4")

        `when`(config.groupCode).thenReturn("grp")
        `when`(config.atolURL).thenReturn("http://atol")

        `when`(cacheManager.getCache("atolToken")).thenReturn(cache)
        `when`(cache.get("token", String::class.java)).thenReturn(null)
        `when`(config.atolLogin).thenReturn("login")
        `when`(config.atolPass).thenReturn("pass")

        val requestBody =
            mapOf(
                "login" to config.atolLogin,
                "pass" to config.atolPass,
            )

        val header =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val entityToken = HttpEntity(requestBody, header)

        `when`(restTemplate.postForEntity("http://atol/getToken", entityToken, TokenResponse::class.java))
            .thenReturn(ResponseEntity.ok(TokenResponse("token123", ErrorInfo("1", 1, "some_error", "error"), "")))

        val headers = HttpHeaders()
        headers.set("Token", "token123")
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity<Unit>(headers)

        val statusResp = AtolStatusResponse("done")
        val url = "http://atol/v4/grp/report/ext-1"

        `when`(restTemplate.exchange(url, HttpMethod.GET, entity, AtolStatusResponse::class.java))
            .thenReturn(ResponseEntity.ok(statusResp))

        val status = atolClient.getPaymentStatus(doc)

        assertEquals("done", status)
    }
}
