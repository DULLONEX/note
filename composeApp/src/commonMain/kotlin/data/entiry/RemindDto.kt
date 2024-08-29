package data.entiry

import config.RemindStatus
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

/**
 * 用于从系统中查询出来
 *
 * @property eventId
 * @property remindDto
 */
data class EventRemind(
    val eventId:String = "",
    var remindDto:RemindDto
)

/**
 * 主要用于页面显示
 *
 * @property id
 * @property event
 * @property title
 * @property time
 * @property details
 * @property status
 */
data class ShowRemind(
    val id: Long = 0,
    val event: String = "",
    val title: String = "",
    val time: String = "",
    val details: String = "",
    val status: RemindStatus = RemindStatus.ONGOING
)
