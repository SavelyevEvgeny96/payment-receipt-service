package ru.sogaz.site.paymentReceiptService.properties
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import ru.sogaz.site.paymentReceiptService.loggerFor

/**
 * Репозиторий информации о приложении.
 */
@ConfigurationProperties(prefix = "app.info")
class AppInfoProperties {
    private val logger = loggerFor(javaClass)

    lateinit var applicationName: String
    lateinit var artifactId: String
    lateinit var groupId: String
    lateinit var description: String
    lateinit var version: String
    lateinit var appProfile: String
    lateinit var javaVersion: String

    @PostConstruct
    fun postConstruct() {
        logger.info("PostConstruct:")
        logger.info("applicationName = " + applicationName)
        logger.info("description = " + description)
        logger.info("version = " + version)
        logger.info("artifactId = " + artifactId)
        logger.info("groupId = " + groupId)
        logger.info("java.version = " + javaVersion)
    }
}
