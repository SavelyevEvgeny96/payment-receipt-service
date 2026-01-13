package ru.sogaz.site.paymentReceiptService.service.atol

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.marcinziolo.kotlin.wiremock.contains
import com.marcinziolo.kotlin.wiremock.equalTo
import com.marcinziolo.kotlin.wiremock.get
import com.marcinziolo.kotlin.wiremock.post
import com.marcinziolo.kotlin.wiremock.returnsJson
import com.marcinziolo.kotlin.wiremock.verify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentReceiptService.model.atol.response.AtolResponse
import ru.sogaz.site.paymentReceiptService.model.atol.response.TokenResponse
import ru.sogaz.site.paymentReceiptService.model.credential.Credentials
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptType
import ru.sogaz.site.paymentReceiptService.model.reference.CompanyData
import ru.sogaz.site.paymentReceiptService.model.reference.ServiceData
import ru.sogaz.site.paymentReceiptService.properties.AtolProperties
import java.time.LocalDateTime
import java.util.UUID

class AtolServiceTest : AtolTests() {
    companion object {
        private const val GET_TOKEN_PATH = "/v4/getToken"
        private const val SEND_RECEIPT_PATH = "/v4/group/sell"
        private const val GET_STATUS_PATH_PATTERN = "/v4/group/report/%s"

        private const val VALID_TOKEN = "valid-token"

        private const val WAIT_STATUS_RESPONSE = "{\"status\": \"wait\"}"
        private const val DONE_STATUS_RESPONSE = "{\"status\": \"done\"}"
        private const val NULL_STATUS_RESPONSE = "{\"status\": \"null\"}"

        private val testCredentials = Credentials("login", "pass")
        private val testCompanyData = CompanyData("email", "inn", "address", "depersonalizedAddress")
        private val testServiceData = ServiceData("url")
    }

    @Autowired
    private lateinit var atolService: AtolService

    @Autowired
    private lateinit var atolProperties: AtolProperties

    private lateinit var wiremock: WireMock

    @RelaxedMockK
    private lateinit var receipt: Receipt

    private lateinit var validUUID: UUID
    private lateinit var validTokenResponseJson: String

    private lateinit var getStatusPath: String

    private lateinit var validAtolResponseJson: String
    private lateinit var emptyUUIDAtolResponseJson: String

    private val objectMapper = ObjectMapper()

    @BeforeAll
    fun beforeAll() {
        initTestProperty(atolProperties)

        validUUID = UUID.randomUUID()
        validTokenResponseJson = TokenResponse(VALID_TOKEN).run(objectMapper::writeValueAsString)

        getStatusPath = GET_STATUS_PATH_PATTERN.format(validUUID)

        validAtolResponseJson = buildValidAtolResponseJson()
        emptyUUIDAtolResponseJson = buildErrorAtolResponseJson()
    }

    @BeforeEach
    fun beforeEach(wmRuntimeInfo: WireMockRuntimeInfo) {
        wiremock = wmRuntimeInfo.wireMock

        wiremock
            .post { url equalTo GET_TOKEN_PATH }
            .returnsJson { body = validTokenResponseJson }

        every { receipt.receiptType } returns ReceiptType.SELL
    }

    @Test
    fun `sendReceipt should return valid response`() {
        wiremock.post {
            url equalTo SEND_RECEIPT_PATH
            headers contains "token" equalTo VALID_TOKEN
        } returnsJson {
            body = validAtolResponseJson
        }

        val responseUUID = atolService.sendReceipt(receipt, testCredentials)

        assertThat(responseUUID).isEqualTo(validUUID)
    }

    @Test
    fun `sendReceipt should return fail status if atol response 400`() {
        wiremock.post {
            url equalTo SEND_RECEIPT_PATH
            headers contains "token" equalTo VALID_TOKEN
        } returnsJson {
            statusCode = 400
            body = emptyUUIDAtolResponseJson
        }

        val responseUUID = atolService.sendReceipt(receipt, testCredentials)

        assertThat(responseUUID).isEqualTo(null)
    }

    @Test
    fun `sendReceipt should throw an error if request end with error`() {
        wiremock
            .post { url equalTo SEND_RECEIPT_PATH }
            .returnsJson { statusCode = 500 }

        assertThrows<InnerException> {
            atolService.sendReceipt(receipt, testCredentials)
        }
    }

    @Test
    fun `sendReceipt should throw an error if token request thrown error`() {
        wiremock
            .post { url equalTo GET_TOKEN_PATH }
            .returnsJson { statusCode = 500 }

        assertThrows<InnerException> {
            atolService.sendReceipt(receipt, testCredentials)
        }

        wiremock.verify {
            urlPath equalTo GET_TOKEN_PATH
            exactly = 1
        }

        wiremock.verify {
            urlPath equalTo SEND_RECEIPT_PATH
            exactly = 0
        }
    }

    @Test
    fun `getStatus should return wait status`() {
        every { receipt.externalId } returns validUUID
        wiremock
            .get { urlPath equalTo getStatusPath }
            .returnsJson { body = WAIT_STATUS_RESPONSE }

        val status = atolService.getStatus(receipt, testCredentials)

        assertThat(status).isEqualTo(ReceiptState.WAIT)
    }

    @Test
    fun `getStatus should return done status`() {
        every { receipt.externalId } returns validUUID
        wiremock
            .get { urlPath equalTo getStatusPath }
            .returnsJson { body = DONE_STATUS_RESPONSE }

        val status = atolService.getStatus(receipt, testCredentials)

        assertThat(status).isEqualTo(ReceiptState.DONE)
    }

    @Test
    fun `getStatus should thrown an error when status invalid`() {
        every { receipt.externalId } returns validUUID
        wiremock
            .post { urlPath equalTo getStatusPath }
            .returnsJson { body = NULL_STATUS_RESPONSE }

        assertThrows<InnerException> {
            atolService.getStatus(receipt, testCredentials)
        }
    }

    private fun buildValidAtolResponseJson() =
        AtolResponse(
            uuid = validUUID,
            status = "wait",
            timestamp = LocalDateTime.now().toString(),
        ).run(objectMapper::writeValueAsString)

    private fun buildErrorAtolResponseJson() = "{\"uuid\":null,\"status\": \"fail\"}"

    private fun initTestProperty(atolProperties: AtolProperties) =
        atolProperties.apply {
            company = testCompanyData
            callback = testServiceData
        }
}
