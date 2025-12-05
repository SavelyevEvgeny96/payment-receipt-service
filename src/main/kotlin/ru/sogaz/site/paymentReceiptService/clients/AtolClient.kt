package ru.sogaz.site.paymentReceiptService.clients

import jakarta.validation.Valid
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import ru.sogaz.site.paymentReceiptService.model.reference.AtolCredentials
import ru.sogaz.site.paymentReceiptService.model.web.request.atol.AtolRequest
import ru.sogaz.site.paymentReceiptService.model.web.response.atol.AtolResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.atol.AtolStatusResponse
import ru.sogaz.site.paymentReceiptService.model.web.response.atol.TokenResponse
import java.util.UUID

@Validated
@FeignClient(
    name = "atol-client",
    url = "\${config.atol.api.basePath}",
)
interface AtolClient {
    @Valid
    @PostMapping(value = ["/v4/getToken"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getToken(
        @RequestBody credentials: AtolCredentials,
    ): TokenResponse

    @Valid
    @PostMapping(value = ["/v4/\${config.atol.api.groupCode}/sell"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendReceipt(
        @RequestHeader token: String,
        @RequestBody request: AtolRequest,
    ): AtolResponse

    @PostMapping(value = ["/v4/\${config.atol.api.groupCode}/report/{externalId}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun getStatus(
        @RequestHeader token: String,
        @PathVariable externalId: UUID,
    ): AtolStatusResponse
}
