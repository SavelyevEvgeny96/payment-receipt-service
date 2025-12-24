package ru.sogaz.site.paymentReceiptService.service.atol

import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.sogaz.site.paymentReceiptService.properties.AtolProperties
import slice.AtolClientTest
import java.net.ServerSocket

@TestInstance(PER_CLASS)
@ExtendWith(SpringExtension::class, MockKExtension::class)
@AtolClientTest
@TestPropertySource("classpath:application-test.properties")
@ComponentScan("ru.sogaz.site.paymentReceiptService.service.atol", "ru.sogaz.site.paymentReceiptService.mapper.atol")
@ContextConfiguration(
    initializers = [AtolTests.Companion.Initializer::class],
    classes = [AtolProperties::class],
)
abstract class AtolTests {
    companion object {
        private val port = ServerSocket(0).localPort

        @RegisterExtension
        @JvmStatic
        private val wireMockExtension =
            WireMockExtension
                .newInstance()
                .options(wireMockConfig().port(port))
                .build()

        class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
            override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
                TestPropertyValues
                    .of(
                        "wiremock.server.port=$port",
                    ).applyTo(configurableApplicationContext.environment)
            }
        }
    }
}
