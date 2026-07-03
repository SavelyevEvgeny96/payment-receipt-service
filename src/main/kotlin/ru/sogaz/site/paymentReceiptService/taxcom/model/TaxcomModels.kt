package ru.sogaz.site.paymentReceiptService.taxcom.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaxcomLoginRequest(
    val login: String,
    val password: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaxcomLoginResponse(
    val sessionToken: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaxcomErrorResponse(
    val details: String? = null,
    val apiErrorCode: Int? = null,
    val commonDescription: String? = null,
    val httpErrorCode: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaxcomCountResponse(
    val recordCount: Int = 0,
    val recordFilteredCount: Int = 0,
    val recordInResponseCount: Int = 0,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaxcomOutletListResponse(
    val reportDate: LocalDateTime? = null,
    val counts: TaxcomCountResponse? = null,
    val records: List<TaxcomOutletRecord> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaxcomOutletRecord(
    val id: UUID,
    val name: String? = null,
    val code: String? = null,
    val address: String? = null,
    val problemIndicator: String? = null,
    val department: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaxcomKktListResponse(
    val reportDate: LocalDateTime? = null,
    val counts: TaxcomCountResponse? = null,
    val outlet: TaxcomOutletRecord? = null,
    val records: List<TaxcomKktRecord> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaxcomKktRecord(
    val name: String? = null,
    val kktRegNumber: String? = null,
    val kktFactoryNumber: String? = null,
    val fnFactoryNumber: String,
    val cashdeskState: String? = null,
    val problemIndicator: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaxcomShiftListResponse(
    val reportDate: LocalDateTime? = null,
    val counts: TaxcomCountResponse? = null,
    val records: List<TaxcomShiftRecord> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaxcomShiftRecord(
    val fnFactoryNumber: String? = null,
    val shiftNumber: Int,
    val openDateTime: LocalDateTime? = null,
    val closeDateTime: LocalDateTime? = null,
    val receiptCount: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaxcomDocumentListResponse(
    val reportDate: LocalDateTime? = null,
    val counts: TaxcomCountResponse? = null,
    val records: List<TaxcomDocumentRecord> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaxcomDocumentRecord(
    val fnFactoryNumber: String,
    val shiftNumber: String? = null,
    val documentType: String? = null,
    val dateTime: LocalDateTime? = null,
    val fdNumber: String,
    val numberInShift: String? = null,
    val fpd: String? = null,
    val cashier: String? = null,
    val taxationSystem: String? = null,
    val accountingType: String? = null,
    val sum: BigDecimal? = null,
    val cash: BigDecimal? = null,
    val electronic: BigDecimal? = null,
    val noncashSum: BigDecimal? = null,
    val nds0: BigDecimal? = null,
    val nds10: BigDecimal? = null,
    val nds18: BigDecimal? = null,
    val nds20: BigDecimal? = null,
    val ndsCalculated10: BigDecimal? = null,
    val ndsCalculated20: BigDecimal? = null,
    val ndsNo: BigDecimal? = null,
    val ndsCalculated: BigDecimal? = null,
    val sumPrepaid: BigDecimal? = null,
    val sumPostpaid: BigDecimal? = null,
    val sumCounterclaims: BigDecimal? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaxcomDocumentInfoResponse(
    val reportDate: LocalDateTime? = null,
    val documentFormatDate: String? = null,
    val documentType: String? = null,
    val documentFormatVersion: String? = null,
    val document: Map<String, Any?> = emptyMap(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaxcomDocumentUrlResponse(
    val taxcomReceiptUrl: String? = null,
)

data class TaxcomOutletRow(
    val id: UUID,
    val name: String?,
)

data class TaxcomKktRow(
    val id: UUID,
    val outletId: UUID,
    val name: String?,
    val numFn: String,
)

data class TaxcomShiftRow(
    val id: UUID,
    val kktId: UUID,
    val num: Int,
    val numFn: String,
)

data class TaxcomReceiptRow(
    val id: UUID,
    val shiftId: UUID,
    val numFn: String,
    val fdNumber: String,
    val documentInfoUploaded: Boolean,
    val documentUrlUploaded: Boolean,
    val subjectsUploaded: Boolean,
)

data class TaxcomRunResponse(
    val accepted: Boolean,
    val status: TaxcomExportStatusResponse,
)

data class TaxcomExportStatusResponse(
    val status: String,
    val running: Boolean,
    val currentStage: String?,
    val startedAt: Instant?,
    val finishedAt: Instant?,
    val processedOutlets: Long,
    val processedKkt: Long,
    val processedShifts: Long,
    val processedReceipts: Long,
    val lastError: String?,
) {
    companion object {
        fun disabled(): TaxcomExportStatusResponse =
            TaxcomExportStatusResponse(
                status = "DISABLED",
                running = false,
                currentStage = null,
                startedAt = null,
                finishedAt = null,
                processedOutlets = 0,
                processedKkt = 0,
                processedShifts = 0,
                processedReceipts = 0,
                lastError = "Taxcom export is disabled. Set TAXCOM_EXPORT_ENABLED=true and restart the service.",
            )
    }
}
