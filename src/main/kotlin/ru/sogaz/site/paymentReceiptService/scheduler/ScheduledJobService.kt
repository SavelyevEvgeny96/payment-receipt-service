package ru.sogaz.site.paymentReceiptService.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.sogaz.site.exceptionStarter.starter.dto.exceptions.BusinessException
import ru.sogaz.site.paymentReceiptService.loggerFor
import ru.sogaz.site.paymentReceiptService.model.web.request.PaymentReceiptUpdateRequest
import ru.sogaz.site.paymentReceiptService.repository.PaymentDocumentRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.CheckStatusRepository
import ru.sogaz.site.paymentReceiptService.repository.reference.ConfigurationDataRepository
import ru.sogaz.site.paymentReceiptService.service.PaymentReceiptService
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class ScheduledJobService(
    private val configurationDataRepository: ConfigurationDataRepository,
    private val paymentDocumentRepository: PaymentDocumentRepository,
    private val paymentReceiptService: PaymentReceiptService,
    private val checkStatusRepository: CheckStatusRepository,
) {
    private val log = loggerFor(javaClass)

    @Scheduled(fixedDelayString = "\${scheduled.task.defaultDelay:60000}")
    fun checkAtolStatuses() {
        val period =
            configurationDataRepository.findByParamName("periodStatusUpdate")?.paramValue?.toLongOrNull()
                ?: return log.warn("Не удалось получить период обновления статусов из конфигурации")

        log.info("Запуск фоновой задачи проверки статусов Атола с периодом: $period секунд")

        val now = LocalDateTime.now()
        val startTime = now.minus(32, ChronoUnit.DAYS)
        val endTime = now.minus(5, ChronoUnit.MINUTES)

        val documents =
            paymentDocumentRepository.findByStatusAndDateSendBetween(
                listOf(
                    checkStatusRepository.findByStateId("new") ?: throw BusinessException(1212),
                    checkStatusRepository.findByStateId("wait") ?: throw BusinessException(1212),
                ),
                startTime,
                endTime,
            )

        if (documents.isEmpty()) {
            log.info("Нет документов для обновления статуса")
            return
        }

        documents.forEach { document ->
            try {
                log.info("Обновление статуса для externalId=${document.externalId}")
                paymentReceiptService.getStatus(
                    PaymentReceiptUpdateRequest(
                        document.externalId ?: throw BusinessException(1212),
                    ),
                )
            } catch (e: Exception) {
                log.error("Ошибка обновления статуса для externalId=${document.externalId}")
            }
        }
    }
}
