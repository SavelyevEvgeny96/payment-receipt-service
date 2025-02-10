package ru.sogaz.site.paymentReceiptService.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.sogaz.site.paymentReceiptService.model.ConfigurationData
import ru.sogaz.site.paymentReceiptService.repository.reference.ConfigurationDataRepository

@Component
class ConfigurationDataLoader(
    private val configurationDataRepository: ConfigurationDataRepository,
) : CommandLineRunner {
    @Value("\${config.atolURL}")
    lateinit var atolURL: String

    @Value("\${config.AtolLogin}")
    lateinit var atolLogin: String

    @Value("\${config.AtolPass}")
    lateinit var atolPass: String

    @Value("\${config.tokenTime}")
    lateinit var tokenTime: String

    @Value("\${config.callbackURL}")
    lateinit var callbackURL: String

    @Value("\${config.companyEmail}")
    lateinit var companyEmail: String

    @Value("\${config.companyInn}")
    lateinit var companyInn: String

    @Value("\${config.paymentAddress}")
    lateinit var paymentAddress: String

    @Value("\${config.groupCode}")
    lateinit var groupCode: String

    @Value("\${config.periodStatusUpdate}")
    lateinit var periodStatusUpdate: String

    @Transactional
    override fun run(vararg args: String?) {
        val configParams =
            mapOf(
                "atolURL" to atolURL,
                "AtolLogin" to atolLogin,
                "AtolPass" to atolPass,
                "tokenTime" to tokenTime,
                "callbackURL" to callbackURL,
                "companyEmail" to companyEmail,
                "companyInn" to companyInn,
                "paymentAddress" to paymentAddress,
                "groupCode" to groupCode,
                "periodStatusUpdate" to periodStatusUpdate,
            )

        configParams.forEach { (key, value) ->
            configurationDataRepository.findByParamName(key)
                ?: configurationDataRepository.save(ConfigurationData(paramName = key, paramValue = value))
        }
    }
}
