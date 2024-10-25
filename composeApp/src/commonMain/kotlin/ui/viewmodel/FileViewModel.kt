package ui.viewmodel

import Platform
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.entiry.FileData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FileViewModel : ViewModel(), KoinComponent {

    val file: MutableSharedFlow<List<FileData>> = MutableStateFlow(emptyList())

    val platform: Platform by inject()

    fun addFileList(files: List<FileData>) {
        viewModelScope.launch {
            file.emit(files)
        }
    }

    fun saveImage(bitmap: ImageBitmap) {
        viewModelScope.launch {
            file.emit(listOf(FileData(null, bitmap)))
        }
    }


}