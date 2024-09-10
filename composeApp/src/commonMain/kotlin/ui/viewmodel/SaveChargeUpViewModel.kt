package ui.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.entiry.AmountTypeDto
import data.entiry.ChargeUpDto
import data.entiry.FileData
import data.service.AmountTypeService
import data.service.ChargeUpService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.error_no_amount1
import note.composeapp.generated.resources.error_no_amount2
import note.composeapp.generated.resources.error_no_content
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SaveChargeUpViewModel : ViewModel(), KoinComponent {
    private val amountService: AmountTypeService by inject()
    private val chargeUpService: ChargeUpService by inject()

    val chargeUpStatus = MutableStateFlow(ChargeUpDto(null))

    val amountTypeList = MutableStateFlow<List<AmountTypeDto>>(emptyList())
    val errorInfo = MutableStateFlow<StringResource?>(null)

    init {
        viewModelScope.launch {
            amountService.findAmountTypeAll().collect {
                amountTypeList.emit(it)
            }
        }
    }

    fun loadChargeUpById(id: Long) {
        viewModelScope.launch {
            chargeUpStatus.emit(chargeUpService.findChargeUpById(id))
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

    fun delFile(imageBitmap: ImageBitmap) {
        viewModelScope.launch {
            val updatedFiles = chargeUpStatus.value.files.toMutableList().apply {
                find { it.imageBitmap == imageBitmap }?.let { remove(it) }
            }
            chargeUpStatus.emit(chargeUpStatus.value.copy(files = updatedFiles))
        }
    }

    fun save(): Boolean {
        val result = CompletableDeferred<Boolean>()

        viewModelScope.launch {
            val errorString = checkAllInputInfo()
            if (errorString == null) {
                if (chargeUpStatus.value.id == null) {
                    chargeUpService.saveChargeUp(chargeUpStatus.value)
                } else {

                }
                result.complete(true)
            } else {
                errorInfo.emit(errorString)
                result.complete(false)
            }
        }

        return runBlocking {
            result.await()
        }
    }

    fun contentChange(value: String) {
        viewModelScope.launch {
            chargeUpStatus.emit(chargeUpStatus.value.copy(content = value))
        }
    }

    private fun checkAllInputInfo(): StringResource? {
        return when {
            chargeUpStatus.value.content.isBlank() -> Res.string.error_no_content
            !isValidAmount(chargeUpStatus.value.amount) -> Res.string.error_no_amount1
            chargeUpStatus.value.amountType.id == 0L -> Res.string.error_no_amount2
            else -> null
        }
    }

    private fun isValidAmount(amount: String): Boolean {
        // 去除可能的逗号
        val sanitizedAmount = amount.replace(",", "")
        return sanitizedAmount.toDoubleOrNull()?.let { it > 0 } ?: false
    }

    fun amountChange(value: String) {
        // 判断是否全是数据
        viewModelScope.launch {
            chargeUpStatus.emit(chargeUpStatus.value.copy(amount = value))
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