package data.entiry

import config.getCurrentDateTime
import config.getCurrentDateTimeAfter
import kotlinx.datetime.LocalDateTime

data class RemindDto(
    val title: String = "",
    val details: String = "",
    val startDate: LocalDateTime = getCurrentDateTime(),
    val endDate: LocalDateTime = getCurrentDateTimeAfter(5),
    val alarm: Boolean = true,
    private val _beforeMinutes: Int = 5 // 使用私有字段
) {
    val beforeMinutes: Int
        get() {
            if (!alarm) {
                return 0
            }
            return _beforeMinutes
        }
}

data class EventRemind(
    val eventId:String = "",
    var remindDto:RemindDto
)