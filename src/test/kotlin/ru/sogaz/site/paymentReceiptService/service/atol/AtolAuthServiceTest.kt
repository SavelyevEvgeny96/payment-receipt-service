package ru.sogaz.site.paymentReceiptService.service.atol

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.marcinziolo.kotlin.wiremock.equalTo
import com.marcinziolo.kotlin.wiremock.post
import com.marcinziolo.kotlin.wiremock.returnsJson
import io.mockk.impl.annotations.RelaxedMockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentReceiptService.model.atol.request.AtolTokenRequest
import ru.sogaz.site.paymentReceiptService.model.atol.response.TokenResponse
import ru.sogaz.site.paymentReceiptService.model.credential.Credentials

class AtolAuthServiceTest : AtolTests() {
    companion object {
        private const val GET_TOKEN_PATH = "/v4/getToken"
        private const val VALID_TOKEN = "valid-token"
        private const val ERROR_TOKEN_RESPONSE = "{\"token\":null,\"error\":null,\"timestamp\":null}"
    }

    @RelaxedMockK
    private lateinit var atolTokenRequest: AtolTokenRequest

    @RelaxedMockK
    private lateinit var credentials: Credentials

    @Autowired
    private lateinit var atolAuthService: AtolAuthService

    private lateinit var wiremock: WireMock

    private lateinit var validTokenResponse: String
    private lateinit var emptyTokenResponse: String

    private val objectMapper = ObjectMapper()

    @BeforeAll
    fun beforeAll() {
        validTokenResponse = TokenResponse(VALID_TOKEN).run(objectMapper::writeValueAsString)
        emptyTokenResponse = TokenResponse("").run(objectMapper::writeValueAsString)
    }

    @BeforeEach
    fun beforeEach(wmRuntimeInfo: WireMockRuntimeInfo) {
        wiremock = wmRuntimeInfo.wireMock
    }

    @Test
    fun `getToken should return valid token`() {
        wiremock
            .post { url equalTo GET_TOKEN_PATH }
            .returnsJson { body = validTokenResponse }

        val token = atolAuthService.getToken(credentials)

        assertThat(token).isEqualTo(VALID_TOKEN)
    }

    @Test
    fun `when getToken return blank token should throw an error`() {
        wiremock
            .post { url equalTo GET_TOKEN_PATH }
            .returnsJson { body = emptyTokenResponse }

        assertThrows<InnerException> {
            atolAuthService.getToken(credentials)
        }
    }

    @Test
    fun `when getToken return null token response should throw an error`() {
        wiremock
            .post { url equalTo GET_TOKEN_PATH }
            .returnsJson { body = ERROR_TOKEN_RESPONSE }

        assertThrows<InnerException> {
            atolAuthService.getToken(credentials)
        }
    }

    @Test
    fun `when atol return http error code should throw an error`() {
        wiremock
            .post { url equalTo GET_TOKEN_PATH }
            .returnsJson { statusCode = 500 }

        assertThrows<InnerException> {
            atolAuthService.getToken(credentials)
        }
    }
}
