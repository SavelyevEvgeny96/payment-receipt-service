package ru.sogaz.site.paymentReceiptService.taxcom.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.LocalDateTime

@ConfigurationProperties(prefix = "taxcom")
class TaxcomProperties {
    var api: Api = Api()
    var datasource: Datasource = Datasource()
    var export: Export = Export()
    var proxy: Proxy = Proxy()

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
    }

    class Export {
        var enabled: Boolean = false
        var begin: LocalDateTime = LocalDateTime.of(2020, 1, 1, 0, 0)
        var batchSize: Int = 500
        var listPageSize: Int = 1500
        var documentPageSize: Int = 1000
        var shiftListWorkers: Int = 5
        var receiptWorkers: Int = 10
        var receiptClaimBatchSize: Int = 20
        var receiptWorkerIdleDelayMs: Long = 2_000
        var receiptLockTimeoutMinutes: Int = 30
        var maxAttempts: Int = 3
    }

    class Proxy {
        var enabled: Boolean = false
        var host: String = ""
        var port: Int = 0
    }
}
