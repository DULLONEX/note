package ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.entiry.AmountTypeDto
import data.service.AmountTypeService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SaveChargeUpViewModel : ViewModel(), KoinComponent {
    private val amountService: AmountTypeService by inject()

    val amountTypeList = MutableStateFlow<List<AmountTypeDto>>(emptyList())

    init {
        viewModelScope.launch {
            amountService.findAmountTypeAll().collect {
                amountTypeList.value = it
            }
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