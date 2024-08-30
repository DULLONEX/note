package ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import config.isNumeric
import data.entiry.AmountTypeDto
import data.entiry.ChargeUpDto
import data.entiry.FileData
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

    fun addFile(fileData: FileData) {
        viewModelScope.launch {
            // 创建一个新的 List，并添加新的文件
            val updatedFiles = chargeUpStatus.value.files.toMutableList().apply {
                add(fileData)
            }
            // 复制新的 List 并更新状态
            chargeUpStatus.emit(chargeUpStatus.value.copy(files = updatedFiles))
        }
    }

    fun contentChange(value: String) {
        viewModelScope.launch {
            chargeUpStatus.emit(chargeUpStatus.value.copy(content = value))
        }
    }

    fun amountChange(value: String) {
        // 判断是否全是数据
        if (isNumeric(value) || value.isEmpty()) {
            viewModelScope.launch {
                chargeUpStatus.emit(chargeUpStatus.value.copy(amount = value))
            }
        }

    }


    fun amountTypeSelect(amountTypeDto: AmountTypeDto) {
        viewModelScope.launch {
            chargeUpStatus.emit(chargeUpStatus.value.copy(amountType = amountTypeDto))
        }
    }

    fun saveAmountType(message: String) {
        viewModelScope.launch {
            amountService.saveAmountType(message)
        }
    }

    fun updateAmountType(id: Long, message: String) {
        viewModelScope.launch {
            amountService.updateAmountType(id, message)
        }
    }


}