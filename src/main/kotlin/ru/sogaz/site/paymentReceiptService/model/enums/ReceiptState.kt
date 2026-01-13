package ru.sogaz.site.paymentReceiptService.model.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class ReceiptState(
    @JsonValue val value: String,
    val desc: String,
) {
    NEW("new", "Создана запись"),
    WAIT("wait", "Отправлен запрос на отправку чека"),
    DONE("done", "Чек отправлен"),
    FAIL("fail", "Ошибка формирования чека"),
    ;

    override fun toString(): String = value
}
