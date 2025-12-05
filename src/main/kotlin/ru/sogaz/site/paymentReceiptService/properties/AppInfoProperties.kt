package ru.sogaz.site.paymentReceiptService.properties
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Репозиторий информации о приложении.
 */
@ConfigurationProperties(prefix = "app.info")
class AppInfoProperties {
    lateinit var applicationName: String
    lateinit var artifactId: String
    lateinit var groupId: String
    lateinit var description: String
    lateinit var version: String
    lateinit var appProfile: String
}
