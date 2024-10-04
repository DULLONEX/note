package ui.screen.chargeUp


import NavCompose
import Platform
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import config.convertLocalDateTime
import config.formatToString
import config.getCurrentDateTime
import config.getStringResource
import config.toLong
import config.withHourAndMinute
import data.entiry.AmountTypeDto
import data.entiry.FileData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.add_image
import note.composeapp.generated.resources.notes
import note.composeapp.generated.resources.payment
import note.composeapp.generated.resources.remark
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ui.AlterSnackbar
import ui.AnimationTextShow
import ui.CenteredTextField
import ui.LoadCompose
import ui.PictureViewer
import ui.TextInputCompose
import ui.screen.remind.DateDayChooseItem
import ui.theme.TransparentOutlinedTextFieldColors
import ui.viewmodel.SaveChargeUpViewModel

class AddChargeUpCompose : KoinComponent {

    private val platform: Platform by inject()

    @Composable
    fun AddChargeUpScreen(
        modifier: Modifier = Modifier,
        navController: NavHostController = platform.navController,
        viewModel: SaveChargeUpViewModel = viewModel { SaveChargeUpViewModel() },
        id: Long? = null
    ) {
        val scope = rememberCoroutineScope()
        val snackarHostState = remember { SnackbarHostState() }
        val errorInfo by viewModel.errorInfo.collectAsState()

        // 加载数据后控制 UI 显示的状态
        var isReadyToShowUI by remember { mutableStateOf(false) }

        // Snackbar 提示
        errorInfo?.let {
            val stringResource = stringResource(it)
            scope.launch {
                snackarHostState.showSnackbar(stringResource)
            }
        }

        LaunchedEffect(id) {
            if (id != null) {
                viewModel.loadChargeUpById(id)
                delay(200)  // 延迟 200 毫秒

            }
            isReadyToShowUI = true  // 只有在延迟后设置状态为 true
        }

        // 如果 UI 准备好才显示界面
        val navCompose = NavCompose()
        Scaffold(modifier,
            snackbarHost = { SnackbarHost(hostState = snackarHostState) { AlterSnackbar(it) } },
            topBar = {
                navCompose.TopAppBarCompose(Modifier.padding(end = 16.dp), onActionClick = {
                    scope.launch {
                        if (viewModel.save()) {
                            navController.navigateUp()
                        }
                    }
                })
            }) { innerPadding ->
            if (isReadyToShowUI) {
                ChargeUpCompose(Modifier.fillMaxSize().padding(innerPadding))
            } else {
                // 显示加载状态
                LoadCompose(Modifier.padding(innerPadding))
            }
        }

    }


    @Composable
    fun ChargeUpCompose(
        modifier: Modifier = Modifier,
        viewModel: SaveChargeUpViewModel = viewModel { SaveChargeUpViewModel() },
    ) {
        val chargeUpStatus by viewModel.chargeUpStatus.collectAsState()
        val amountTypeList by viewModel.amountTypeList.collectAsState()

        Column(
            modifier
        ) {
            HeadCompose(
                modifier = Modifier,
                textValue = chargeUpStatus.content,
                textOnChange = viewModel::contentChange,
                amountValue = chargeUpStatus.amount,
                amountOnChange = viewModel::amountChange,
                selectedDate = chargeUpStatus.fillTime,
                onDateChange = viewModel::fillTimeChange
            )
            //类型
            AmountTypeCompose(
                Modifier.padding(horizontal = 8.dp),
                amountTypeList = amountTypeList,
                addAmountType = viewModel::saveAmountType,
                updateAmountType = viewModel::updateAmountType,
                selected = chargeUpStatus.amountType,
                selectOnChange = viewModel::amountTypeSelect
            )
            //选择图片
            platform.SelectImageCompose(fileDataList = chargeUpStatus.files,
                addFile = { viewModel.addFile(FileData(null, it)) },
                delFile = { image -> viewModel.delFile(image) },
                content = { imageBitmapArray, addPhoto, delImageChange ->
                    SelectedFileImageCompose(
                        Modifier.padding(horizontal = 8.dp),
                        imageBitmapArray = imageBitmapArray,
                        addPhoto = addPhoto,
                        delImageChange = delImageChange
                    )
                })
        }
    }

}


@Composable
fun SelectedFileImageCompose(
    modifier: Modifier = Modifier,
    imageBitmapArray: List<ImageBitmap>,
    addPhoto: () -> Unit,
    delImageChange: (ImageBitmap) -> Unit
) {
    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    Column(modifier.fillMaxWidth()) {
        Button({ addPhoto() }) {
            Text(stringResource(Res.string.add_image))
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(imageBitmapArray) {
                Box(Modifier.size(135.dp, 240.dp)) {
                    Image(it, "", modifier = Modifier.clickable {
                        selectedImage = it
                    })
                    IconButton(
                        {
                            delImageChange(it)
                        }, modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete, ""
                        )
                    }
                }

            }
        }
    }

    if (selectedImage != null) {
        PictureViewer(onDismissRequest = { selectedImage = null }, bitmap = selectedImage!!)
    }

}


@Composable
fun AmountTypeCompose(
    modifier: Modifier = Modifier,
    amountTypeList: List<AmountTypeDto> = emptyList(),
    addAmountType: (String) -> Unit = {},
    updateAmountType: (Long, String) -> Unit = { _, _ -> },
    selected: AmountTypeDto = AmountTypeDto(0, "", false),
    selectOnChange: (AmountTypeDto) -> Unit = {}
) {
    var showSheet by remember { mutableStateOf(false) }

    //  var selectedType by remember { mutableStateOf(selected) }

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(75.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(amountTypeList, key = { it.message }) { item ->

            AmountTypeItem(amountTypeDto = item,
                onClick = {
                    selectOnChange(item)
                },
                selectedId = selected.id,
                textModifier = Modifier.heightIn(max = 50.dp).pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        // 普通点击事件
                        selectOnChange(item)
                    }, onLongPress = {
                        // 长按事件
                        // val currentType = amountTypeList.findLast { it.id == item.id }!!
                        if (!item.whetherSystem) {
                            selectOnChange(item)
                            showSheet = !showSheet
                        }
                    })
                })

        }
        item {
            // 用于添加的item
            AmountTypeAddItem(addAmountType = { dto ->
                addAmountType(dto.message)
            })
        }
    }

    //显示修改以
    if (showSheet) {
        AmountTypeBottomSheet(Modifier, onDone = {
            updateAmountType(it.id, it.message)
        }, onDismiss = { showSheet = !showSheet }, amountType = selected)
    }
}

@Composable
fun AmountTypeAddItem(
    modifier: Modifier = Modifier, addAmountType: (AmountTypeDto) -> Unit = {}
) {
    var showSheet by remember { mutableStateOf(false) }
    FilterChip(modifier = modifier, onClick = {
        // 打开sheet 进行填写
        showSheet = !showSheet
    }, label = {
        Box(
            modifier = Modifier.fillMaxWidth(), // 让 Box 填充整个可用宽度
            contentAlignment = Alignment.Center // 将内容（Icon）居中对齐
        ) {
            Icon(
                imageVector = Icons.Default.Add, contentDescription = "add type"
            )
        }
    }, selected = false
    )
    if (showSheet) {
        AmountTypeBottomSheet(
            modifier = Modifier,
            onDone = addAmountType,
            onDismiss = { showSheet = !showSheet })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountTypeBottomSheet(
    modifier: Modifier = Modifier,
    onDone: (AmountTypeDto) -> Unit = {},
    onDismiss: () -> Unit = {},
    amountType: AmountTypeDto = AmountTypeDto(0, "", false)
) {
    var text by remember { mutableStateOf(amountType.message) }
    ModalBottomSheet(modifier = modifier.heightIn(min = 300.dp), onDismissRequest = {
        onDismiss()
    }) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CenteredTextField(
                text = text, onTextChange = { text = it }, modifier = Modifier
            )
            Spacer(Modifier.heightIn(40.dp))
            Button({
                onDone(amountType.copy(message = text))
                onDismiss()
            }) {
                Icon(Icons.Default.Done, "done")
            }
        }
    }
}


@Composable
fun AmountTypeItem(
    filterChipModifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    amountTypeDto: AmountTypeDto,
    onClick: () -> Unit,
    selectedId: Long
) {
    FilterChip(
        modifier = filterChipModifier, onClick = onClick, label = {
            Box(textModifier) {
                Text(
                    if (amountTypeDto.whetherSystem) stringResource(getStringResource(amountTypeDto.message)) else amountTypeDto.message,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

        }, selected = amountTypeDto.id == selectedId
    )

}

/**
 * 头部用于结合输入和金钱选择
 *
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeadCompose(
    modifier: Modifier = Modifier,
    textValue: String = "",
    textOnChange: (String) -> Unit = {},
    amountValue: String = "",
    amountOnChange: (String) -> Unit = {},
    selectedDate: LocalDateTime = getCurrentDateTime(),
    onDateChange: (LocalDateTime) -> Unit = {},
) {
    var dateSelect by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    LaunchedEffect(dateSelect) {
        datePickerState.apply { this.selectedDateMillis = selectedDate.toLong() }
    }
    DateDayChooseItem(dateSelect = dateSelect, dateSelectChange = {
        dateSelect = !dateSelect
    }, selectedDateChange = {
        // 时分 不用修改
        onDateChange(
            convertLocalDateTime(it).withHourAndMinute(
                selectedDate.hour, selectedDate.minute
            )
        )
    }, datePickerState = datePickerState)

    Column(modifier.fillMaxWidth()) {
        // 用于填写消费日期
        Text(
            text = selectedDate.formatToString(), modifier = Modifier.padding(start = 4.dp).clickable {
                dateSelect = !dateSelect
            }, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 32.sp)
        )
        HorizontalDivider()

        /**
         * 备注用于填写此部消费备注信息
         */
        TextInputCompose(
            value = textValue,
            onChange = textOnChange,
            textStyle = MaterialTheme.typography.headlineMedium,
            placeholderCompose = {
                Text(
                    stringResource(Res.string.remark),
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            leadingIconCompose = {
                Icon(painter = painterResource(Res.drawable.notes), "info")
            },
            singleLine = false
        )
        HorizontalDivider()
        /**
         * 金额
         */
        AmountInputCompose(
            modifier.fillMaxWidth().align(Alignment.End),
            value = amountValue,
            onChange = amountOnChange
        )
    }
}

/**
 * 用于金额输入的input
 *
 */
@Composable
fun AmountInputCompose(
    modifier: Modifier = Modifier,
    value: String,
    onChange: (String) -> Unit,
    textStyle: TextStyle = LocalTextStyle.current,

    ) {
    val outline = MaterialTheme.colorScheme.outline
    OutlinedTextField(
        modifier = modifier.drawWithContent {
            drawContent()
            drawLine(
                color = outline,
                start = Offset(x = 0f, y = size.height),
                end = Offset(x = size.width, y = size.height),
                strokeWidth = 2f
            )
        },
        value = value,
        onValueChange = { newValue ->
            if (newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {  // Allows numbers with optional decimal point
                onChange(newValue)
            }
        },
        placeholder = {
            Text("0.00", style = textStyle)
        },
        leadingIcon = {
            Icon(painter = painterResource(Res.drawable.payment), "money")
        },
        textStyle = textStyle,
        singleLine = true,
        colors = MaterialTheme.colorScheme.TransparentOutlinedTextFieldColors,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal  // Allows decimal point input
        )
    )
}

