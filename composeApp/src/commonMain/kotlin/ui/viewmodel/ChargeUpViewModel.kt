package ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import config.YearMonth
import data.entiry.ChargeUpDto
import data.entiry.MonthSumCharge
import data.entiry.SimpleChargeUpSheet
import data.entiry.TypeStatistic
import data.service.ChargeUpService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChargeUpViewModel : ViewModel(), KoinComponent {

    val chargeUpMap: MutableStateFlow<Map<MonthSumCharge, List<ChargeUpDto>>> = MutableStateFlow(
        hashMapOf()
    )

    val simpleSheet: MutableStateFlow<SimpleChargeUpSheet> = MutableStateFlow(SimpleChargeUpSheet())

    private val currentMonth = MutableStateFlow<YearMonth?>(null)
    private val isAll = MutableStateFlow(true)

    @OptIn(ExperimentalCoroutinesApi::class)
    val statisticsList: StateFlow<List<TypeStatistic>> =
        combine(currentMonth, isAll) { month, all ->
            month to all
        }
            .flatMapLatest { (month, all) ->
                if (all) {
                    chargeUpService.getAllChargeUpStatisticsGroupType()
                } else {
                    chargeUpService.getAllChargeUpStatisticsGroupTypeBySomeMonth(
                        month!!.year,
                        month.month  // 如果是 YearMonth 用 monthNumber
                    )
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
    private val chargeUpService: ChargeUpService by inject()


    init {
        viewModelScope.launch {
            chargeUpService.findAllChargeUp().collect {
                chargeUpMap.emit(it)
            }
        }
        // 简单基本信息
        viewModelScope.launch {
            chargeUpService.findSimpleCharUpSheet().collect {
                simpleSheet.emit(it)
            }
        }

        // 统计信息
    }

    fun getStatistics(yearMonth: YearMonth, all: Boolean): Unit {
        currentMonth.value = yearMonth
        isAll.value = all
    }

    fun delById(id: Long) {
        viewModelScope.launch {
            chargeUpService.deleteChargeUp(id)
        }
    }
}