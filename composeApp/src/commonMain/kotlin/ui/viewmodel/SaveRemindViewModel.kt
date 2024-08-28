package ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import config.addDay
import config.addTime
import config.differDay
import config.differMinute
import data.entiry.RemindDto
import data.service.RemindService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.error_time1
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SaveRemindViewModel : ViewModel(), KoinComponent {

    private val remindService: RemindService by inject()

    private val _uiState = MutableStateFlow(RemindDto())

    val uiState = _uiState.asStateFlow()

    fun titleChange(value: String) {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(title = value))
        }
    }

    fun detailsChange(value: String) {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(details = value))
        }
    }

    fun startDateChange(value: LocalDateTime) {
        val remind = _uiState.value
        viewModelScope.launch {
            // 先获取日期差值 天 / 分钟
            endDateChange(
                value.addDay(remind.endDate.differDay(remind.startDate).toInt())
                    .addTime(remind.endDate.differMinute(remind.startDate).toInt())
            )
            _uiState.emit(remind.copy(startDate = value))
        }
    }

    fun endDateChange(value: LocalDateTime) {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(endDate = value))
        }
    }

    fun alarmChange(value: Boolean) {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(alarm = value))
        }
    }

    fun beforeMinutesChange(value: Int) {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(_beforeMinutes = value))
        }
    }

    fun saveRemind() {
        // 添加日历信息
        val remind = _uiState.value

        viewModelScope.launch {
            if (checkRemind() == null) {
                remindService.saveRemind(remind)
                // 刷新一波
                remindService.initialLoad()
            }
        }
    }

    fun checkRemind(): StringResource? {
        val addRemind = _uiState.value
        if (addRemind.startDate > addRemind.endDate) {
            return Res.string.error_time1
        }
        return null
    }

}


