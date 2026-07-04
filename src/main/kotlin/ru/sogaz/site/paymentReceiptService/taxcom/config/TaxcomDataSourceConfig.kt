package ru.sogaz.site.paymentReceiptService.taxcom.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
@ConditionalOnProperty(prefix = "taxcom.export", name = ["enabled"], havingValue = "true")
@EnableConfigurationProperties(TaxcomProperties::class)
class TaxcomDataSourceConfig(
    private val taxcomProperties: TaxcomProperties,
) {
    @Bean
    @Primary
    fun dataSource(dataSourceProperties: DataSourceProperties): DataSource =
        dataSourceProperties.initializeDataSourceBuilder().build()

    @Bean
    fun taxcomDataSource(): DataSource {
        return HikariDataSource().apply {
            driverClassName = MSSQL_DRIVER_CLASS_NAME
            jdbcUrl = taxcomProperties.datasource.url
            username = taxcomProperties.datasource.username
            password = taxcomProperties.datasource.password
            poolName = "taxcom-db-pool"
        }
    }

    @Bean
    fun taxcomJdbcTemplate(
        @Qualifier("taxcomDataSource") dataSource: DataSource,
    ): JdbcTemplate = JdbcTemplate(dataSource)

    @Bean
    fun taxcomNamedParameterJdbcTemplate(
        @Qualifier("taxcomDataSource") dataSource: DataSource,
    ): NamedParameterJdbcTemplate = NamedParameterJdbcTemplate(dataSource)

    @Bean
    fun taxcomTransactionManager(
        @Qualifier("taxcomDataSource") dataSource: DataSource,
    ): PlatformTransactionManager = DataSourceTransactionManager(dataSource)

    companion object {
        private const val MSSQL_DRIVER_CLASS_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
    }
}
