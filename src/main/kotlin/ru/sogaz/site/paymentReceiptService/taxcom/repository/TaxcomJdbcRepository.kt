package ru.sogaz.site.paymentReceiptService.taxcom.repository

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomDocumentInfoResponse
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomDocumentRecord
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomKktRecord
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomKktRow
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomOutletRecord
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomOutletRow
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomReceiptRow
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomShiftRecord
import ru.sogaz.site.paymentReceiptService.taxcom.model.TaxcomShiftRow
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

@Repository
@ConditionalOnProperty(prefix = "taxcom.job", name = ["enabled"], havingValue = "true")
class TaxcomJdbcRepository(
    private val taxcomJdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) {
    fun ensureSchema() {
        DDL.forEach(taxcomJdbcTemplate::execute)
    }

    fun upsertOutlets(records: List<TaxcomOutletRecord>) = records.forEach { record ->
        taxcomJdbcTemplate.update(
            """
            MERGE list_outlets AS target
            USING (SELECT ? AS id, ? AS name) AS source
            ON target.id = source.id
            WHEN MATCHED THEN UPDATE SET name = source.name, date_update = SYSUTCDATETIME()
            WHEN NOT MATCHED THEN INSERT (id, name, data_uploaded, date_create, date_update)
            VALUES (source.id, source.name, 0, SYSUTCDATETIME(), SYSUTCDATETIME());
            """.trimIndent(),
            record.id,
            record.name,
        )
    }

    fun findPendingOutlets(limit: Int): List<TaxcomOutletRow> =
        taxcomJdbcTemplate.query(
            "SELECT TOP (?) id, name FROM list_outlets WHERE data_uploaded = 0 ORDER BY date_create",
            { rs, _ -> TaxcomOutletRow(UUID.fromString(rs.getString("id")), rs.getString("name")) },
            limit,
        )

    fun upsertKkt(outletId: UUID, records: List<TaxcomKktRecord>) = records.forEach { record ->
        taxcomJdbcTemplate.update(
            """
            MERGE list_kkt AS target
            USING (SELECT ? AS list_outlets_id, ? AS num_fn, ? AS name) AS source
            ON target.list_outlets_id = source.list_outlets_id AND target.num_fn = source.num_fn
            WHEN MATCHED THEN UPDATE SET name = source.name, date_update = SYSUTCDATETIME()
            WHEN NOT MATCHED THEN INSERT (id, list_outlets_id, name, num_fn, data_uploaded, date_create, date_update)
            VALUES (NEWID(), source.list_outlets_id, source.name, source.num_fn, 0, SYSUTCDATETIME(), SYSUTCDATETIME());
            """.trimIndent(),
            outletId,
            record.fnFactoryNumber,
            record.name,
        )
    }

    fun markOutletUploadedIfDone(outletId: UUID) {
        taxcomJdbcTemplate.update(
            """
            UPDATE list_outlets
            SET data_uploaded = 1, date_update = SYSUTCDATETIME(), last_error = NULL
            WHERE id = ? AND NOT EXISTS (
                SELECT 1 FROM list_kkt WHERE list_outlets_id = ? AND data_uploaded = 0
            )
            """.trimIndent(),
            outletId,
            outletId,
        )
    }

    fun markOutletUploaded(outletId: UUID) = markUploaded("list_outlets", outletId)

    fun findPendingKkt(limit: Int): List<TaxcomKktRow> =
        taxcomJdbcTemplate.query(
            "SELECT TOP (?) id, list_outlets_id, name, num_fn FROM list_kkt WHERE data_uploaded = 0 ORDER BY date_create",
            { rs, _ ->
                TaxcomKktRow(
                    UUID.fromString(rs.getString("id")),
                    UUID.fromString(rs.getString("list_outlets_id")),
                    rs.getString("name"),
                    rs.getString("num_fn"),
                )
            },
            limit,
        )

    fun upsertShifts(kktId: UUID, records: List<TaxcomShiftRecord>) = records.forEach { record ->
        taxcomJdbcTemplate.update(
            """
            MERGE list_shifts AS target
            USING (SELECT ? AS list_kkt_id, ? AS num) AS source
            ON target.list_kkt_id = source.list_kkt_id AND target.num = source.num
            WHEN MATCHED THEN UPDATE SET date_update = SYSUTCDATETIME()
            WHEN NOT MATCHED THEN INSERT (id, list_kkt_id, num, data_uploaded, date_create, date_update)
            VALUES (NEWID(), source.list_kkt_id, source.num, 0, SYSUTCDATETIME(), SYSUTCDATETIME());
            """.trimIndent(),
            kktId,
            record.shiftNumber,
        )
    }

    fun markKktUploadedIfDone(kktId: UUID) {
        taxcomJdbcTemplate.update(
            """
            UPDATE list_kkt
            SET data_uploaded = 1, date_update = SYSUTCDATETIME(), last_error = NULL
            WHERE id = ? AND NOT EXISTS (
                SELECT 1 FROM list_shifts WHERE list_kkt_id = ? AND data_uploaded = 0
            )
            """.trimIndent(),
            kktId,
            kktId,
        )
    }

    fun markKktUploaded(kktId: UUID) = markUploaded("list_kkt", kktId)

    fun findPendingShifts(limit: Int): List<TaxcomShiftRow> =
        taxcomJdbcTemplate.query(
            """
            SELECT TOP (?) s.id, s.list_kkt_id, s.num, k.num_fn
            FROM list_shifts s
            JOIN list_kkt k ON k.id = s.list_kkt_id
            WHERE s.data_uploaded = 0
            ORDER BY s.date_create
            """.trimIndent(),
            { rs, _ ->
                TaxcomShiftRow(
                    UUID.fromString(rs.getString("id")),
                    UUID.fromString(rs.getString("list_kkt_id")),
                    rs.getInt("num"),
                    rs.getString("num_fn"),
                )
            },
            limit,
        )

    fun upsertDocuments(shiftId: UUID, records: List<TaxcomDocumentRecord>) = records.forEach { record ->
        taxcomJdbcTemplate.update(
            """
            MERGE receipts_taxcom AS target
            USING (SELECT ? AS list_shifts_id, ? AS tag_1042) AS source
            ON target.list_shifts_id = source.list_shifts_id AND target.tag_1042 = source.tag_1042
            WHEN MATCHED THEN UPDATE SET
                document_type = ?, document_datetime = ?, number_in_shift = ?, fpd = ?, cashier = ?, taxation_system = ?,
                accounting_type = ?, total_sum = ?, cash_sum = ?, electronic_sum = ?, noncash_sum = ?, nds0 = ?, nds10 = ?,
                nds18 = ?, nds20 = ?, nds_calculated10 = ?, nds_calculated20 = ?, nds_no = ?, nds_calculated = ?,
                sum_prepaid = ?, sum_postpaid = ?, sum_counterclaims = ?, raw_document_list = ?, date_update = SYSUTCDATETIME()
            WHEN NOT MATCHED THEN INSERT (
                id, list_shifts_id, tag_1042, document_type, document_datetime, number_in_shift, fpd, cashier, taxation_system,
                accounting_type, total_sum, cash_sum, electronic_sum, noncash_sum, nds0, nds10, nds18, nds20, nds_calculated10,
                nds_calculated20, nds_no, nds_calculated, sum_prepaid, sum_postpaid, sum_counterclaims, raw_document_list,
                data_uploaded, document_info_uploaded, document_url_uploaded, subjects_uploaded, date_create, date_update
            ) VALUES (NEWID(), source.list_shifts_id, source.tag_1042, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 0, 0, SYSUTCDATETIME(), SYSUTCDATETIME());
            """.trimIndent(),
            shiftId,
            record.fdNumber,
            record.documentType,
            record.dateTime?.toTimestamp(),
            record.numberInShift,
            record.fpd,
            record.cashier,
            record.taxationSystem,
            record.accountingType,
            record.sum,
            record.cash,
            record.electronic,
            record.noncashSum,
            record.nds0,
            record.nds10,
            record.nds18,
            record.nds20,
            record.ndsCalculated10,
            record.ndsCalculated20,
            record.ndsNo,
            record.ndsCalculated,
            record.sumPrepaid,
            record.sumPostpaid,
            record.sumCounterclaims,
            objectMapper.writeValueAsString(record),
            record.documentType,
            record.dateTime?.toTimestamp(),
            record.numberInShift,
            record.fpd,
            record.cashier,
            record.taxationSystem,
            record.accountingType,
            record.sum,
            record.cash,
            record.electronic,
            record.noncashSum,
            record.nds0,
            record.nds10,
            record.nds18,
            record.nds20,
            record.ndsCalculated10,
            record.ndsCalculated20,
            record.ndsNo,
            record.ndsCalculated,
            record.sumPrepaid,
            record.sumPostpaid,
            record.sumCounterclaims,
            objectMapper.writeValueAsString(record),
        )
    }

    fun findPendingReceipts(limit: Int): List<TaxcomReceiptRow> =
        taxcomJdbcTemplate.query(
            """
            SELECT TOP (?) r.id, r.list_shifts_id, k.num_fn, r.tag_1042, r.document_info_uploaded, r.document_url_uploaded, r.subjects_uploaded
            FROM receipts_taxcom r
            JOIN list_shifts s ON s.id = r.list_shifts_id
            JOIN list_kkt k ON k.id = s.list_kkt_id
            WHERE r.data_uploaded = 0
            ORDER BY r.date_create
            """.trimIndent(),
            { rs, _ -> rs.toReceiptRow() },
            limit,
        )

    fun updateDocumentInfo(receiptId: UUID, response: TaxcomDocumentInfoResponse) {
        val document = response.document
        taxcomJdbcTemplate.update(
            """
            UPDATE receipts_taxcom SET
                document_format_date = ?, document_format_version = ?, document_type = COALESCE(?, document_type),
                tag_1054 = ?, tag_1031 = ?, tag_1021 = ?, tag_1012 = ?, tag_1081 = ?, tag_1040 = ?, tag_1077 = ?,
                tag_1018 = ?, tag_1037 = ?, tag_1105 = ?, tag_1055 = ?, tag_1038 = ?, tag_1041 = ?, tag_1203 = ?,
                tag_1215 = ?, tag_1216 = ?, tag_1217 = ?, tag_1059 = ?, tag_1209 = ?, raw_document_info = ?,
                document_info_uploaded = 1, date_update = SYSUTCDATETIME(), last_error = NULL
            WHERE id = ?
            """.trimIndent(),
            response.documentFormatDate,
            response.documentFormatVersion,
            response.documentType,
            document["1054"]?.toString(),
            document["1031"]?.toString(),
            document["1021"]?.toString(),
            document["1012"]?.toString(),
            document["1081"]?.toString(),
            document["1040"]?.toString(),
            document["1077"]?.toString(),
            document["1018"]?.toString(),
            document["1037"]?.toString(),
            document["1105"]?.toString(),
            document["1055"]?.toString(),
            document["1038"]?.toString(),
            document["1041"]?.toString(),
            document["1203"]?.toString(),
            document["1215"]?.toString(),
            document["1216"]?.toString(),
            document["1217"]?.toString(),
            objectMapper.writeValueAsString(document["1059"]),
            document["1209"]?.toString(),
            objectMapper.writeValueAsString(response),
            receiptId,
        )
        replaceSubjects(receiptId, document["1059"])
    }

    fun updateDocumentUrl(receiptId: UUID, taxcomReceiptUrl: String?, rawResponse: Any) {
        taxcomJdbcTemplate.update(
            """
            UPDATE receipts_taxcom SET taxcom_receipt_url = ?, raw_document_url = ?, document_url_uploaded = 1,
                date_update = SYSUTCDATETIME(), last_error = NULL
            WHERE id = ?
            """.trimIndent(),
            taxcomReceiptUrl,
            objectMapper.writeValueAsString(rawResponse),
            receiptId,
        )
    }

    fun markReceiptUploadedIfDone(receiptId: UUID) {
        taxcomJdbcTemplate.update(
            """
            UPDATE receipts_taxcom
            SET data_uploaded = 1, date_update = SYSUTCDATETIME(), last_error = NULL
            WHERE id = ? AND document_info_uploaded = 1 AND document_url_uploaded = 1 AND subjects_uploaded = 1
            """.trimIndent(),
            receiptId,
        )
    }

    fun markShiftUploadedIfDone(shiftId: UUID) {
        taxcomJdbcTemplate.update(
            """
            UPDATE list_shifts
            SET data_uploaded = 1, date_update = SYSUTCDATETIME(), last_error = NULL
            WHERE id = ? AND NOT EXISTS (
                SELECT 1 FROM receipts_taxcom WHERE list_shifts_id = ? AND data_uploaded = 0
            )
            """.trimIndent(),
            shiftId,
            shiftId,
        )
    }

    fun markShiftUploaded(shiftId: UUID) = markUploaded("list_shifts", shiftId)

    fun saveError(table: String, id: UUID, message: String?) {
        taxcomJdbcTemplate.update(
            "UPDATE $table SET last_error = ?, load_attempts = load_attempts + 1, date_update = SYSUTCDATETIME() WHERE id = ?",
            message?.take(MAX_ERROR_LENGTH),
            id,
        )
    }

    private fun replaceSubjects(receiptId: UUID, subjects: Any?) {
        taxcomJdbcTemplate.update("DELETE FROM subject_calculation WHERE id_receipts_taxcom = ?", receiptId)
        val subjectList = subjects as? List<*> ?: emptyList<Any>()
        subjectList.forEachIndexed { index, item ->
            val subject = item as? Map<*, *> ?: emptyMap<Any, Any>()
            taxcomJdbcTemplate.update(
                """
                INSERT INTO subject_calculation (
                    id, id_receipts_taxcom, line_number, tag_1023, tag_1079, tag_1043, tag_1030, tag_1199, tag_1212,
                    raw_subject, date_create
                ) VALUES (NEWID(), ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSUTCDATETIME())
                """.trimIndent(),
                receiptId,
                index + 1,
                subject["1023"]?.toString(),
                subject["1079"]?.toString(),
                subject["1043"]?.toString(),
                subject["1030"]?.toString(),
                subject["1199"]?.toString(),
                subject["1212"]?.toString(),
                objectMapper.writeValueAsString(subject),
            )
        }
        taxcomJdbcTemplate.update(
            "UPDATE receipts_taxcom SET subjects_uploaded = 1, date_update = SYSUTCDATETIME() WHERE id = ?",
            receiptId,
        )
    }

    private fun markUploaded(table: String, id: UUID) {
        taxcomJdbcTemplate.update(
            "UPDATE $table SET data_uploaded = 1, date_update = SYSUTCDATETIME(), last_error = NULL WHERE id = ?",
            id,
        )
    }

    private fun ResultSet.toReceiptRow(): TaxcomReceiptRow =
        TaxcomReceiptRow(
            UUID.fromString(getString("id")),
            UUID.fromString(getString("list_shifts_id")),
            getString("num_fn"),
            getString("tag_1042"),
            getBoolean("document_info_uploaded"),
            getBoolean("document_url_uploaded"),
            getBoolean("subjects_uploaded"),
        )

    private fun LocalDateTime.toTimestamp(): Timestamp = Timestamp.valueOf(this)

    companion object {
        private const val MAX_ERROR_LENGTH = 4000

        private val DDL = listOf(
            """
            IF OBJECT_ID('list_outlets', 'U') IS NULL CREATE TABLE list_outlets (
                id uniqueidentifier NOT NULL PRIMARY KEY,
                name nvarchar(1000) NULL,
                data_uploaded bit NOT NULL DEFAULT 0,
                load_attempts int NOT NULL DEFAULT 0,
                last_error nvarchar(4000) NULL,
                date_create datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
                date_update datetime2 NULL
            )
            """.trimIndent(),
            """
            IF OBJECT_ID('list_kkt', 'U') IS NULL CREATE TABLE list_kkt (
                id uniqueidentifier NOT NULL PRIMARY KEY,
                list_outlets_id uniqueidentifier NOT NULL,
                name nvarchar(1000) NULL,
                num_fn nvarchar(100) NOT NULL,
                data_uploaded bit NOT NULL DEFAULT 0,
                load_attempts int NOT NULL DEFAULT 0,
                last_error nvarchar(4000) NULL,
                date_create datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
                date_update datetime2 NULL,
                CONSTRAINT uq_list_kkt_outlet_fn UNIQUE (list_outlets_id, num_fn)
            )
            """.trimIndent(),
            """
            IF OBJECT_ID('list_shifts', 'U') IS NULL CREATE TABLE list_shifts (
                id uniqueidentifier NOT NULL PRIMARY KEY,
                list_kkt_id uniqueidentifier NOT NULL,
                num int NOT NULL,
                data_uploaded bit NOT NULL DEFAULT 0,
                load_attempts int NOT NULL DEFAULT 0,
                last_error nvarchar(4000) NULL,
                date_create datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
                date_update datetime2 NULL,
                CONSTRAINT uq_list_shifts_kkt_num UNIQUE (list_kkt_id, num)
            )
            """.trimIndent(),
            """
            IF OBJECT_ID('receipts_taxcom', 'U') IS NULL CREATE TABLE receipts_taxcom (
                id uniqueidentifier NOT NULL PRIMARY KEY,
                list_shifts_id uniqueidentifier NOT NULL,
                document_format_date nvarchar(50) NULL,
                document_format_version nvarchar(50) NULL,
                document_type nvarchar(50) NULL,
                document_datetime datetime2 NULL,
                number_in_shift nvarchar(50) NULL,
                fpd nvarchar(100) NULL,
                cashier nvarchar(1000) NULL,
                taxation_system nvarchar(100) NULL,
                accounting_type nvarchar(100) NULL,
                total_sum decimal(19, 4) NULL,
                cash_sum decimal(19, 4) NULL,
                electronic_sum decimal(19, 4) NULL,
                noncash_sum decimal(19, 4) NULL,
                nds0 decimal(19, 4) NULL,
                nds10 decimal(19, 4) NULL,
                nds18 decimal(19, 4) NULL,
                nds20 decimal(19, 4) NULL,
                nds_calculated10 decimal(19, 4) NULL,
                nds_calculated20 decimal(19, 4) NULL,
                nds_no decimal(19, 4) NULL,
                nds_calculated decimal(19, 4) NULL,
                sum_prepaid decimal(19, 4) NULL,
                sum_postpaid decimal(19, 4) NULL,
                sum_counterclaims decimal(19, 4) NULL,
                tag_1054 nvarchar(max) NULL,
                tag_1031 nvarchar(max) NULL,
                tag_1042 nvarchar(100) NOT NULL,
                tag_1021 nvarchar(max) NULL,
                tag_1012 nvarchar(max) NULL,
                tag_1081 nvarchar(max) NULL,
                tag_1040 nvarchar(max) NULL,
                tag_1077 nvarchar(max) NULL,
                tag_1018 nvarchar(max) NULL,
                tag_1037 nvarchar(max) NULL,
                tag_1105 nvarchar(max) NULL,
                tag_1055 nvarchar(max) NULL,
                tag_1038 nvarchar(max) NULL,
                tag_1041 nvarchar(max) NULL,
                tag_1203 nvarchar(max) NULL,
                tag_1215 nvarchar(max) NULL,
                tag_1216 nvarchar(max) NULL,
                tag_1217 nvarchar(max) NULL,
                tag_1059 nvarchar(max) NULL,
                tag_1209 nvarchar(max) NULL,
                taxcom_receipt_url nvarchar(2000) NULL,
                raw_document_list nvarchar(max) NULL,
                raw_document_info nvarchar(max) NULL,
                raw_document_url nvarchar(max) NULL,
                data_uploaded bit NOT NULL DEFAULT 0,
                document_info_uploaded bit NOT NULL DEFAULT 0,
                document_url_uploaded bit NOT NULL DEFAULT 0,
                subjects_uploaded bit NOT NULL DEFAULT 0,
                load_attempts int NOT NULL DEFAULT 0,
                last_error nvarchar(4000) NULL,
                date_create datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
                date_update datetime2 NULL,
                CONSTRAINT uq_receipts_taxcom_shift_fd UNIQUE (list_shifts_id, tag_1042)
            )
            """.trimIndent(),
            """
            IF OBJECT_ID('subject_calculation', 'U') IS NULL CREATE TABLE subject_calculation (
                id uniqueidentifier NOT NULL PRIMARY KEY,
                id_receipts_taxcom uniqueidentifier NOT NULL,
                line_number int NOT NULL,
                tag_1023 nvarchar(max) NULL,
                tag_1079 nvarchar(max) NULL,
                tag_1043 nvarchar(max) NULL,
                tag_1030 nvarchar(max) NULL,
                tag_1199 nvarchar(max) NULL,
                tag_1212 nvarchar(max) NULL,
                raw_subject nvarchar(max) NULL,
                date_create datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
                CONSTRAINT uq_subject_calculation_receipt_line UNIQUE (id_receipts_taxcom, line_number)
            )
            """.trimIndent(),
        )
    }
}
