package ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.entiry.AmountTypeDto
import data.entiry.ChargeUpDto
import data.service.AmountTypeService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SaveChargeUpViewModel : ViewModel(), KoinComponent {
    private val amountService: AmountTypeService by inject()

    val chargeUpStatus = MutableStateFlow(ChargeUpDto())

    val amountTypeList = MutableStateFlow<List<AmountTypeDto>>(emptyList())

    init {
        viewModelScope.launch {
            amountService.findAmountTypeAll().collect {
                amountTypeList.emit(it)
            }
        }
    }

    fun amountTypeSelect(amountTypeDto: AmountTypeDto){
        viewModelScope.launch {
            chargeUpStatus.emit(chargeUpStatus.value.copy(amountType = amountTypeDto))
        }
    }

    fun saveAmountType(message:String) {
        viewModelScope.launch {
            amountService.saveAmountType(message)
        }
    }

    fun updateAmountType(id:Long,message:String) {
        viewModelScope.launch {
            amountService.updateAmountType(id,message)
        }
    }


}