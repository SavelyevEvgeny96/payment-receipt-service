package ru.sogaz.site.paymentReceiptService.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.sogaz.site.paymentReceiptService.loggerFor
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptUpdateRequest
import ru.sogaz.site.paymentReceiptService.properties.ConfigurationDataProperties
import ru.sogaz.site.paymentReceiptService.repository.PaymentDocumentRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.CheckStatusRepository
import ru.sogaz.site.paymentReceiptService.service.PaymentReceiptService
import java.time.LocalDateTime

@Component
class ScheduledJobService(
    private val configurationDataProperties: ConfigurationDataProperties,
    private val paymentDocumentRepository: PaymentDocumentRepository,
    private val paymentReceiptService: PaymentReceiptService,
    private val checkStatusRepository: CheckStatusRepository,
) {
    private val log = loggerFor(javaClass)

    @Scheduled(fixedDelayString = "\${scheduled.task.defaultDelay}")
    fun checkAtolStatuses() {
        val period = configurationDataProperties.periodStatusUpdate
        log.info("Запуск фоновой задачи проверки статусов Атола с периодом: $period секунд")

        val now = LocalDateTime.now()
        val startTime = now.minusDays(32)
        val endTime = now.minusMinutes(5)

        val newStatus = checkStatusRepository.findByStateId("new")
        val waitStatus = checkStatusRepository.findByStateId("wait")

        if (newStatus == null || waitStatus == null) {
            log.warn("Не найдены статусы 'new' или 'wait' — задача пропущена")
            return
        }

        val statuses = listOf(newStatus, waitStatus)

        val documents =
            try {
                paymentDocumentRepository.findByStatusAndDateSendBetween(statuses, startTime, endTime)
            } catch (ex: Exception) {
                log.error(ex, "Ошибка при получении документов из БД")
                return
            }

        if (documents.isEmpty()) {
            log.info("Нет документов для обновления статуса")
            return
        }

        documents.forEach { document ->
            try {
                val externalId = document.externalId
                if (externalId == null) {
                    log.warn("Документ с id=${document.docId} не содержит externalId — пропущен")
                    return@forEach
                }

                log.info("Обновление статуса для externalId=$externalId")
                paymentReceiptService.getStatus(PaymentReceiptUpdateRequest(externalId))
            } catch (e: Exception) {
                log.error(e, "Ошибка обновления статуса для externalId=${document.externalId}")
            }
        }
    }
}
