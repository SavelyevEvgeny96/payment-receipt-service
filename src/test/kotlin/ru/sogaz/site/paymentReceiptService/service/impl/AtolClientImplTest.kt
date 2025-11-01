package ru.sogaz.site.paymentReceiptService.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
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
    lateinit var objectMapper: ObjectMapper

    @Mock
    lateinit var cache: Cache

    @Mock
    lateinit var config: ConfigurationDataProperties

    @InjectMocks
    lateinit var atolClient: AtolClientImpl

    @Test
    fun `токен возвращается из кэша`() {
        `when`(cacheManager.getCache("atolToken")).thenReturn(cache)
        `when`(cache.get("token", String::class.java)).thenReturn("cached-token")

        val token = atolClient.getAtolToken("v4")

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
            ResponseEntity.ok(TokenResponse("new-token", null, "20-10-2025"))
        `when`(
            restTemplate.postForEntity("http://atol/v4/getToken", entityToken, TokenResponse::class.java),
        ).thenReturn(response)

        val token = atolClient.getAtolToken("v4")

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
            atolClient.getAtolToken("v4")
        }
    }

    @Test
    fun `успешная отправка чека возвращает externalId`() {
        val doc = mock(PaymentDocument::class.java)
        val item = mock(PaymentItem::class.java)
        val payment = mock(PaymentReceipt::class.java)

        val docId = UUID.randomUUID()
        `when`(doc.docId).thenReturn(docId)
        `when`(doc.clientEmail).thenReturn("user@mail.com")
        `when`(doc.clientPhone).thenReturn("123456")
        `when`(doc.total).thenReturn(100.0)

        `when`(config.atolURL).thenReturn("http://atol")
        `when`(config.groupCode).thenReturn("grp")
        `when`(config.callbackURL).thenReturn("http://callback")
        `when`(config.companyEmail).thenReturn("comp@mail.com")
        `when`(config.companyInn).thenReturn("1234567890")
        `when`(config.paymentAddress).thenReturn("Address")
        `when`(config.atolLogin).thenReturn("login")
        `when`(config.atolPass).thenReturn("pass")

        `when`(cacheManager.getCache("atolToken")).thenReturn(cache)
        `when`(cache.get("token", String::class.java)).thenReturn(null)

        val method = mock(PaymentMethod::class.java)
        val obj = mock(PaymentObject::class.java)
        val vat = mock(VatType::class.java)
        `when`(item.name).thenReturn("item1")
        `when`(item.price).thenReturn(10.0)
        `when`(item.quantity).thenReturn(1.0)
        `when`(item.sum).thenReturn(10.0)
        `when`(item.paymentMethod).thenReturn(method)
        `when`(item.paymentObject).thenReturn(obj)
        `when`(item.vatType).thenReturn(vat)
        `when`(method.paymentMethodCode).thenReturn("full_payment")
        `when`(obj.paymentObjectIdCode).thenReturn("commodity")
        `when`(vat.vatTypeCode).thenReturn("vat20")

        val type = mock(PaymentType::class.java)
        `when`(payment.sum).thenReturn(10.0)
        `when`(payment.paymentType).thenReturn(type)
        `when`(type.typeIdCode).thenReturn(1)

        val tokenResponse = TokenResponse("token123", null, "24.07.2025 12:00:00")

        `when`(
            restTemplate.postForEntity(
                eq("http://atol/v4/getToken"),
                any(),
                eq(TokenResponse::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(tokenResponse))

        val uuid = "external-uuid-123"
        val atolResponse = AtolResponse(uuid, "wait", null, "24.07.2025 12:00:00")

        `when`(
            restTemplate.postForEntity(
                eq("http://atol/v4/grp/sell?token=token123"),
                any(),
                eq(AtolResponse::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(atolResponse))

        val result = atolClient.sendAtolRequest(doc, listOf(item), listOf(payment), "v4")

        assertEquals(uuid, result)
    }

    @Test
    fun `getPaymentStatus возвращает статус`() {
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

        `when`(restTemplate.postForEntity("http://atol/v4/getToken", entityToken, TokenResponse::class.java))
            .thenReturn(ResponseEntity.ok(TokenResponse("token123", ErrorInfo("1", 1, "some_error", "error"), "")))

        val headers = HttpHeaders()
        headers.set("Token", "token123")
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity<Unit>(headers)

        val statusResp = AtolStatusResponse("done")
        val url = "http://atol/v4/grp/report/ext-1"

        `when`(restTemplate.exchange(url, HttpMethod.GET, entity, AtolStatusResponse::class.java))
            .thenReturn(ResponseEntity.ok(statusResp))

        val status = atolClient.getPaymentStatus("ext-1", "v4")

        assertEquals("done", status)
    }
}
