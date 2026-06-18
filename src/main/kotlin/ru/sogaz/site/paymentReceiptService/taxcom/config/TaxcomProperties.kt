package ru.sogaz.site.paymentReceiptService.taxcom.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.LocalDateTime

@ConfigurationProperties(prefix = "taxcom")
class TaxcomProperties {
    var api: Api = Api()
    var datasource: Datasource = Datasource()
    var export: Export = Export()

    class Api {
        lateinit var baseUrl: String
        lateinit var integratorId: String
        lateinit var login: String
        lateinit var password: String
    }

    class Datasource {
        lateinit var url: String
        lateinit var username: String
        lateinit var password: String
        var driverClassName: String = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
    }

    class Export {
        var enabled: Boolean = false
        var begin: LocalDateTime = LocalDateTime.of(2020, 1, 1, 0, 0)
        var batchSize: Int = 500
    }
}
