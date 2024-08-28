package config

import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.done
import note.composeapp.generated.resources.not_state
import note.composeapp.generated.resources.ongoing

enum class RemindStatus(val value: Long) {
    NOT_STATE(0),    // 未开始
    ONGOING(1),     // 进行中
    DONE(2);        // 已经完成
    companion object {
        // 根据 Long 值查找对应的 enum
        fun fromValue(value: Long): RemindStatus {
            return entries.find { it.value == value }!!
        }
    }
}

val listTab = mutableListOf(Res.string.not_state,
    Res.string.ongoing,
    Res.string.done,)