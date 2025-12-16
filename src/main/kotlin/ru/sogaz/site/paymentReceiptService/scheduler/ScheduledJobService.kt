package ru.sogaz.site.paymentReceiptService.scheduler

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.sogaz.site.exceptionStarter.starter.config.loggerFor
import ru.sogaz.site.paymentReceiptService.dao.ReceiptDao
import ru.sogaz.site.paymentReceiptService.model.entity.Receipt
import ru.sogaz.site.paymentReceiptService.model.enums.ReceiptState
import ru.sogaz.site.paymentReceiptService.producer.ReceiptEventsProducer
import ru.sogaz.site.paymentReceiptService.service.receipt.ReceiptStatusService
import java.time.LocalDateTime

@Component
class ScheduledJobService(
    private val receiptDao: ReceiptDao,
    private val receiptStatusService: ReceiptStatusService,
    private val receiptEventsProducer: ReceiptEventsProducer,
) {
    companion object {
        private const val OLDEST_RECEIPTS_DAYS: Long = 32
        private const val NEWEST_RECEIPTS_MINUTES: Long = 5
    }

    private val log = loggerFor(javaClass)

    @Scheduled(cron = "\${scheduled.task.cron}")
    @SchedulerLock(
        name = "checkAtolStatuses",
        lockAtLeastFor = "PT30S",
    )
    fun checkAtolStatuses() =
        findDocumentForUpdateStatus()
            .mapNotNull(::updateStatusForPaymentDocument)
            .filter(::receiptHasFinalStatus)
            .forEach(receiptEventsProducer::receiptSentEvent)

    private fun findDocumentForUpdateStatus(): List<Receipt> =
        try {
            val now = LocalDateTime.now()
            val startTime = now.minusDays(OLDEST_RECEIPTS_DAYS)
            val endTime = now.minusMinutes(NEWEST_RECEIPTS_MINUTES)

            receiptDao.findByStatusAndDateSendBetween(ReceiptState.WAIT, startTime, endTime)
        } catch (ex: Exception) {
            log.error("Ошибка при получении документов из БД", ex)
            emptyList()
        }

    private fun updateStatusForPaymentDocument(document: Receipt): Receipt? {
        try {
            val externalId = document.externalId
            if (externalId == null) {
                log.warn("Документ с id=${document.id} не содержит externalId — пропущен")
                return null
            }

            log.debug("Обновление статуса для externalId=$externalId")
            return receiptStatusService.updateStatusFromAtol(document)
        } catch (ex: Exception) {
            log.error("Ошибка обновления статуса для externalId=${document.externalId}", ex)
            return null
        }
    }

    private fun receiptHasFinalStatus(receipt: Receipt): Boolean =
        receipt.state in listOf(ReceiptState.DONE, ReceiptState.FAIL)
}
