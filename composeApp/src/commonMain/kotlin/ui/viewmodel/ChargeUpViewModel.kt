package ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.entiry.ChargeUpDto
import data.entiry.MonthSumCharge
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
    private val charService: ChargeUpService by inject()


    init {
        viewModelScope.launch {
            charService.findAllChargeUp().collect {
                chargeUpMap.emit(it)
            }
        }
    }

    fun delById(id: Long) {
        viewModelScope.launch {
            charService.deleteChargeUp(id)
        }
    }
}