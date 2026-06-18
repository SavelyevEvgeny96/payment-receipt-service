package ru.sogaz.site.paymentReceiptService.taxcom.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
@ConditionalOnProperty(prefix = "taxcom.export", name = ["enabled"], havingValue = "true")
@EnableConfigurationProperties(TaxcomProperties::class)
class TaxcomMsSqlDataSourceConfig(
    private val taxcomProperties: TaxcomProperties,
) {
    @Bean
    fun taxcomDataSource(): DataSource =
        HikariDataSource().apply {
            jdbcUrl = taxcomProperties.datasource.url
            username = taxcomProperties.datasource.username
            password = taxcomProperties.datasource.password
            driverClassName = taxcomProperties.datasource.driverClassName
            poolName = "taxcom-ms-sql-pool"
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
}
