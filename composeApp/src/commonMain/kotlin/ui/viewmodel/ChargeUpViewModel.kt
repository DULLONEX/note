package ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.entiry.ChargeUpDto
import data.entiry.MonthSumCharge
import data.entiry.SimpleChargeUpSheet
import data.service.ChargeUpService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChargeUpViewModel : ViewModel(), KoinComponent {

    val chargeUpMap: MutableStateFlow<Map<MonthSumCharge, List<ChargeUpDto>>> = MutableStateFlow(
        hashMapOf()
    )

    val simpleSheet: MutableStateFlow<SimpleChargeUpSheet> = MutableStateFlow(SimpleChargeUpSheet())

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
    }

    fun delById(id: Long) {
        viewModelScope.launch {
            chargeUpService.deleteChargeUp(id)
        }
    }
}