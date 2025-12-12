package ru.sogaz.site.paymentReceiptService.service.credentials

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.InnerException
import ru.sogaz.site.paymentReceiptService.dao.CheckoutMappingDao
import ru.sogaz.site.paymentReceiptService.mapper.credentials.CredentialsMapper
import ru.sogaz.site.paymentReceiptService.mapper.credentials.CredentialsMapperImpl
import ru.sogaz.site.paymentReceiptService.model.entity.CheckoutMapping
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.service.credentials.impl.CredentialsEncryptorImpl
import ru.sogaz.site.paymentReceiptService.service.credentials.impl.CredentialsEncryptorImpl.Companion.ENCRYPT_ERROR_MESSAGE
import ru.sogaz.site.paymentReceiptService.service.credentials.impl.CredentialsManagerImpl
import ru.sogaz.site.paymentReceiptService.service.credentials.impl.CredentialsManagerImpl.Companion.NOT_FOUND_CREDENTIALS_ERROR_MESSAGE
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables
import uk.org.webcompere.systemstubs.jupiter.SystemStub
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension

@TestInstance(PER_CLASS)
@ExtendWith(MockKExtension::class, SpringExtension::class, SystemStubsExtension::class)
@Import(value = [CredentialsMapperImpl::class, CredentialsEncryptorImpl::class])
class AtolCredentialsManagerTest {
    companion object {
        private const val ALL = "ALL"
        private const val PRODUCT_1 = "PRODUCT_1"
        private const val CHANNEL_1 = "CHANNEL_1"
        private const val PRODUCT_2 = "PRODUCT_2"
        private const val CHANNEL_2 = "CHANNEL_2"
        private const val CHANNEL_3 = "CHANNEL_3"
        private const val UNKNOWN_PRODUCT = "UNKNOWN_PRODUCT"
        private const val UNKNOWN_CHANNEL = "UNKNOWN_CHANNEL"
    }

    @SystemStub
    private lateinit var environment: EnvironmentVariables

    @MockK
    private lateinit var checkoutMappingDao: CheckoutMappingDao

    @Autowired
    private lateinit var credentialsEncryptor: CredentialsEncryptor

    @Autowired
    private lateinit var credentialsMapper: CredentialsMapper

    private lateinit var credentialsManager: CredentialsManagerImpl

    private lateinit var credentialCheckouts: List<CheckoutMapping>

    @RelaxedMockK
    private lateinit var receipt: Receipt

    @BeforeEach
    fun beforeEach() {
        credentialsManager = initManager()
        credentialCheckouts = initTestCheckoutMappings()
        initEnv(credentialCheckouts)

        every { checkoutMappingDao.findAll() } returns credentialCheckouts
    }

    @Test
    fun `findCredentials should throw an error when env are empty`() {
        environment.remove("$PRODUCT_1$CHANNEL_1")
        environment.remove("$CHANNEL_1$PRODUCT_1")

        val ex =
            assertThrows<InnerException> {
                credentialsManager.findCredentials(PRODUCT_1, CHANNEL_1)
            }

        assertThat(ex.message).isEqualTo(ENCRYPT_ERROR_MESSAGE)
    }

    @Test
    fun `findCredentials should throw an error when database are empty`() {
        every { checkoutMappingDao.findAll() } returns emptyList()

        val ex =
            assertThrows<InnerException> {
                credentialsManager.findCredentials(PRODUCT_1, CHANNEL_1)
            }

        assertThat(ex.message).isEqualTo(NOT_FOUND_CREDENTIALS_ERROR_MESSAGE)
    }

    @Test
    fun `findCredentials should return default credentials when product and channel is null`() {
        val credentials = credentialsManager.findCredentials(null, null)

        assertThat(credentials)
            .isNotNull
            .returns("$ALL$ALL") { it?.login }
            .returns("$ALL$ALL") { it?.pass }
    }

    @Test
    fun `findCredentials should return default credentials when credentials for product and channel is missing`() {
        val credentials = credentialsManager.findCredentials(UNKNOWN_PRODUCT, UNKNOWN_CHANNEL)

        assertThat(credentials)
            .isNotNull
            .returns("$ALL$ALL") { it?.login }
            .returns("$ALL$ALL") { it?.pass }
    }

    @Test
    fun `findCredentials should correctly return credentials from env`() {
        val credentials = credentialsManager.findCredentials(PRODUCT_1, CHANNEL_1)

        assertThat(credentials)
            .isNotNull
            .returns("$PRODUCT_1$CHANNEL_1") { it?.login }
            .returns("$CHANNEL_1$PRODUCT_1") { it?.pass }
    }

    @Test
    fun `findCredentials should return default for product credentials when credentials for channel is missing`() {
        val credentials = credentialsManager.findCredentials(PRODUCT_2, UNKNOWN_CHANNEL)

        assertThat(credentials)
            .isNotNull
            .returns("$PRODUCT_2$ALL") { it?.login }
            .returns("$ALL$PRODUCT_2") { it?.pass }
    }

    @Test
    fun `findCredentials should return default for channel credentials when credentials for product is missing`() {
        val credentials = credentialsManager.findCredentials(UNKNOWN_PRODUCT, CHANNEL_3)

        assertThat(credentials)
            .isNotNull
            .returns("$ALL$CHANNEL_3") { it?.login }
            .returns("$CHANNEL_3$ALL") { it?.pass }
    }

    @Test
    fun `findCredentials should return credentials for product with priority over the channel`() {
        val credentials = credentialsManager.findCredentials(PRODUCT_2, CHANNEL_3)

        assertThat(credentials)
            .isNotNull
            .returns("$PRODUCT_2$ALL") { it?.login }
            .returns("$ALL$PRODUCT_2") { it?.pass }
    }

    @Test
    fun `findCredentials should return credentials by receipt`() {
        every { receipt.product } returns PRODUCT_1
        every { receipt.channel } returns CHANNEL_1

        val credentials = credentialsManager.findCredentials(receipt)

        assertThat(credentials)
            .isNotNull
            .returns("$PRODUCT_1$CHANNEL_1") { it?.login }
            .returns("$CHANNEL_1$PRODUCT_1") { it?.pass }
    }

    private fun initTestCheckoutMappings(): List<CheckoutMapping> =
        listOf(
            createTestCheckoutMapping(PRODUCT_1, CHANNEL_1),
            createTestCheckoutMapping(PRODUCT_1, CHANNEL_2),
            createTestCheckoutMapping(PRODUCT_2, CHANNEL_1),
            createTestCheckoutMapping(PRODUCT_2, ALL),
            createTestCheckoutMapping(ALL, CHANNEL_3),
            createTestCheckoutMapping(ALL, ALL),
        )

    private fun createTestCheckoutMapping(
        product: String?,
        channel: String?,
    ) = CheckoutMapping(
        product = product,
        channel = channel,
        login = "$product$channel",
        password = "$channel$product",
    )

    private fun initEnv(checkouts: List<CheckoutMapping>) {
        checkouts.forEach(::initEnv)
    }

    private fun initEnv(checkoutMapping: CheckoutMapping) {
        environment.set(checkoutMapping.login, checkoutMapping.login)
        environment.set(checkoutMapping.password, checkoutMapping.password)
    }

    private fun initManager() =
        CredentialsManagerImpl(
            checkoutMappingDao,
            credentialsEncryptor,
            credentialsMapper,
        )
}
