package ru.sogaz.site.paymentReceiptService.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.sogaz.site.paymentReceiptService.properties.AppInfoProperties

private const val SCHEMA = "bearer"
private const val SECURITY_SCHEME_NAME = "bearerAuth"
private const val BEARER_FORMAT = "JWT"

@Configuration
open class SwaggerConfig(
    private val appInfo: AppInfoProperties,
) {
    @Bean
    open fun customOpenAPI(): OpenAPI =
        OpenAPI().apply {
            addSecurityItem(
                SecurityRequirement().apply {
                    addList(SECURITY_SCHEME_NAME)
                },
            )
            // Компоненты Swagger для схемы безопасности
            components =
                Components().apply {
                    addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        SecurityScheme().apply {
                            name(SECURITY_SCHEME_NAME)
                            type(SecurityScheme.Type.HTTP)
                            scheme(SCHEMA)
                            bearerFormat(BEARER_FORMAT)
                        },
                    )
                }
            // информация о приложении
            info =
                Info().apply {
                    title = appInfo.applicationName
                    description = "${appInfo.description} <br>Среда:" +
                        " ${appInfo.appProfile} <br> GroupId: " +
                        "${appInfo.groupId} ArtefactId: " +
                        appInfo.artifactId
                    version = appInfo.version
                }
        }
}
