package ui.viewmodel

import Platform
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import config.RemindStatus
import config.Timer
import config.getCurrentDateTimeLong
import data.service.RemindService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RemindViewModel : ViewModel(), KoinComponent {
    private val remindService: RemindService by inject()
    private val platform: Platform by inject()

    val showRemind: MutableStateFlow<Map<RemindStatus, List<ShowRemind>>> =
        MutableStateFlow(hashMapOf())


    suspend fun refresh() {
        val timer = Timer()
        timer.start()
        remindService.initialLoad()
        loadRemindList()
        val elapsedTime = timer.stop()
        if (elapsedTime < 1000) {
            delay(1000 - elapsedTime)
        }
    }

    suspend fun loadRemindList() {
        val language = platform.getCurrentLanguage()
        viewModelScope.launch {
            remindService.findRemindAll(language).collect{
                showRemind.value = it
            }
        }
    }

    suspend fun remindStatusUpdate(id: Long, remindStatus: RemindStatus) {
        viewModelScope.launch {
            // 获取记录
            var remind = remindService.findRemindById(id)
            val differ = remind.endDate - remind.startDate
            when (remindStatus) {
                config.RemindStatus.ONGOING -> {
                    remind = remind.copy(
                        startDate = getCurrentDateTimeLong(),
                        endDate = getCurrentDateTimeLong() + differ
                    )
                }

                config.RemindStatus.DONE -> {
                    remind = remind.copy(
                        startDate = getCurrentDateTimeLong() - differ,
                        endDate = getCurrentDateTimeLong()
                    )
                }

                else -> {
                }
            }
            remindService.updateRemindStatus(id, remindStatus, remind.startDate, remind.endDate)

            // loadRemindList()
        }
    }

    suspend fun delById(id: Long) {
        viewModelScope.launch {
            remindService.delRemindById(id)
            // loadRemindList()
        }
    }

}

data class ShowRemind(
    val id: Long = 0,
    val event: String = "",
    val title: String = "",
    val time: String = "",
    val details: String = "",
    val status: RemindStatus = RemindStatus.ONGOING
)
