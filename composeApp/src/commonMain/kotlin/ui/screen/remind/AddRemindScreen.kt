package ui.screen.remind

import NavCompose
import Platform
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import config.convertLocalDateTime
import config.formatTimeToString
import config.formatToString
import config.getCurrentDateTime
import config.toLong
import config.withHourAndMinute
import data.entiry.RemindDto
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.add_details
import note.composeapp.generated.resources.add_title
import note.composeapp.generated.resources.cancel
import note.composeapp.generated.resources.error
import note.composeapp.generated.resources.error_time1
import note.composeapp.generated.resources.minutes_before
import note.composeapp.generated.resources.notes
import note.composeapp.generated.resources.save
import note.composeapp.generated.resources.whether_to_remind
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ui.AnimationTextShow
import ui.LookOut
import ui.NumberPicker
import ui.TextInputCompose
import ui.theme.timeShadow
import ui.viewmodel.SaveRemindViewModel


class AddRemindScreenCompose : KoinComponent {
    private val platform: Platform by inject()

    @Composable
    fun AddRemindScreen(
        modifier: Modifier = Modifier,
        viewModel: SaveRemindViewModel = viewModel { SaveRemindViewModel() },
        navController: NavHostController = platform.navController
    ) {

        val navCompose = NavCompose()
        val state = viewModel.uiState.collectAsState()
        val addRemind = state.value
        var errorMessage by remember { mutableStateOf(Res.string.error_time1) }
        var isError by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()


        Scaffold(modifier, topBar = {
            navCompose.TopAppBarCompose(Modifier.padding(end = 16.dp), onActionClick = {
                scope.launch {
                    val error = viewModel.checkRemind()
                    if (error == null) {
                        viewModel.saveRemind()
                        navController.navigateUp()
                    } else {
                        errorMessage = error
                        isError = true
                    }
                }
            })
        }) { innerPadding ->
            SaveRemindCompose(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                remindDto = addRemind,
                titleChange = viewModel::titleChange,
                detailsChange = viewModel::detailsChange,
                startDateChange = viewModel::startDateChange,
                endDateChange = viewModel::endDateChange,
                alarmChange = viewModel::alarmChange,
                beforeMinutesChange = viewModel::beforeMinutesChange
            )

            LookOut(message = errorMessage, isShow = isError, onShowChange = {
                isError = !isError
            })
        }
    }
}

@Composable
fun SaveRemindCompose(
    modifier: Modifier = Modifier,
    remindDto: RemindDto,
    titleChange: (String) -> Unit,
    detailsChange: (String) -> Unit,
    startDateChange: (LocalDateTime) -> Unit,
    endDateChange: (LocalDateTime) -> Unit,
    alarmChange: (Boolean) -> Unit,
    beforeMinutesChange: (Int) -> Unit,
) {
    val isErrorTime by derivedStateOf { remindDto.startDate > remindDto.endDate }

    Column(
        modifier, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 头部输入框
        HeadTextInput(Modifier, remindDto, titleChange, detailsChange)
        // 日期选择 开始-结束
        ChooseDateTimeCompose(
            selectedDate = remindDto.startDate,
            onDateChange = startDateChange,
            isError = isErrorTime
        )
        ChooseDateTimeCompose(
            selectedDate = remindDto.endDate, onDateChange = endDateChange
        )
        // 设置多少分钟前提醒
        Spacer(Modifier.height(10.dp))
        BeforeSetReMind(
            Modifier, remindDto.alarm, alarmChange, remindDto.beforeMinutes, beforeMinutesChange
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeforeSetReMind(
    modifier: Modifier = Modifier,
    isAlarm: Boolean = true,
    onAlarmChange: (Boolean) -> Unit = {},
    beforeMinutes: Int = 5,
    onBeforeMinutesChange: (Int) -> Unit = {}
) {
    var isChecked by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    Row(
        modifier.fillMaxWidth().padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.whether_to_remind))
            Switch(
                modifier = Modifier.graphicsLayer(
                    scaleX = 0.75f,  // Scale width
                    scaleY = 0.75f   // Scale height
                ), checked = isAlarm, onCheckedChange = onAlarmChange
            )
        }
        Row {
            AnimatedVisibility(isAlarm) {
                Text("$beforeMinutes ${stringResource(Res.string.minutes_before)}",
                    modifier = Modifier.clickable {
                        isChecked = !isChecked
                    })
            }
        }
    }
    AnimatedVisibility(isChecked) {
        ModalBottomSheet(sheetState = sheetState,
            modifier = Modifier.height(250.dp),
            onDismissRequest = {
                isChecked = !isChecked
            }) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NumberPicker(
                    defaultSelectedNumber = beforeMinutes,
                    onChange = onBeforeMinutesChange,
                    numbers = (1..120)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseDateTimeCompose(
    modifier: Modifier = Modifier,
    selectedDate: LocalDateTime = getCurrentDateTime(),
    onDateChange: (LocalDateTime) -> Unit = {},
    isError: Boolean = false
) {
    // 用于控制选择天数
    var dateSelect by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(dateSelect) {
        datePickerState.apply { this.selectedDateMillis = selectedDate.toLong() }
    }

    // 选择天
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

    // 具体时/分

    // 用于控制选择时分
    var dateSelectTime by remember { mutableStateOf(false) }

    MinuteChooseItem(
        modifier = Modifier,
        dateSelect = dateSelectTime,
        dateSelectChange = { dateSelectTime = !dateSelectTime },
        selectedDateChange = { hour, minute ->
            onDateChange(selectedDate.withHourAndMinute(hour, minute))
        },
        selectedDate.hour,
        selectedDate.minute
    )

    // 在对话框下方显示选定的日期
    Row(
        modifier = modifier.fillMaxWidth()
            .background(color = if (isError) Color(252, 232, 231) else Color.Transparent)
            .padding(horizontal = 50.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 第一个 Text，靠左
        AnimationTextShow(
            text = selectedDate.formatToString(), modifier = Modifier.weight(1f).clickable {
                dateSelect = !dateSelect
            }, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp)
        )
        if (isError) {
            Box(
                modifier = Modifier.absoluteOffset(x = (-200).dp, y = 0.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.error), // 替换为你的图标资源
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(197, 34, 31)
                )
            }
        }

        // 第二个 Text，靠右
        AnimationTextShow(
            text = selectedDate.formatTimeToString(), modifier = Modifier.weight(1f).clickable {
                dateSelectTime = !dateSelectTime
            }, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp)
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinuteChooseItem(
    modifier: Modifier = Modifier,
    dateSelect: Boolean = false,
    dateSelectChange: () -> Unit,
    selectedDateChange: (Int, Int) -> Unit,
    hour: Int = 0,
    minute: Int = 0

) {
    val rememberCoroutineScope = rememberCoroutineScope()

    val rememberTimePickerState =
        remember(hour, minute, dateSelect) { TimePickerState(hour, minute, true) }

    if (dateSelect) {
        DatePickerDialog(onDismissRequest = {
            dateSelectChange()
        }, confirmButton = {
            TextButton(onClick = {
                dateSelectChange()
            }) {
                Text(stringResource(Res.string.cancel))
            }
            TextButton(onClick = {
                rememberCoroutineScope.launch {
                    dateSelectChange()
                    selectedDateChange(
                        rememberTimePickerState.hour, rememberTimePickerState.minute
                    )
                }

            }) {
                Text(stringResource(Res.string.save))
            }
        }, modifier = modifier.timeShadow()
        ) {
            TimePicker(
                rememberTimePickerState,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 15.dp)
            )
        }
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateDayChooseItem(
    modifier: Modifier = Modifier,
    dateSelect: Boolean = false,
    dateSelectChange: () -> Unit,
    selectedDateChange: (Long?) -> Unit,
    datePickerState: DatePickerState = rememberDatePickerState()
) {
    val rememberCoroutineScope = rememberCoroutineScope()

    AnimatedVisibility(
        dateSelect, exit = shrinkOut(animationSpec = tween(0)) + fadeOut(animationSpec = tween(0))
    ) {
        DatePickerDialog(onDismissRequest = {
            rememberCoroutineScope.launch {
                dateSelectChange()
            }
        }, confirmButton = {
            TextButton(onClick = {
                dateSelectChange()
            }) {
                Text(stringResource(Res.string.cancel))
            }
            TextButton(onClick = {
                rememberCoroutineScope.launch {
                    dateSelectChange()
                    selectedDateChange(datePickerState.selectedDateMillis)
                }
            }) {
                Text(stringResource(Res.string.save))
            }
        }, shape = RoundedCornerShape(30.dp), modifier = modifier.timeShadow()
        ) {
            DatePicker(datePickerState, showModeToggle = false)
        }
    }
}


/**
 * 头部输入框
 * 包含(标题,详情信息)
 *
 * @param modifier
 * @param remindDto
 * @param titleChange
 * @param detailsChange
 */
@Composable
fun HeadTextInput(
    modifier: Modifier = Modifier,
    remindDto: RemindDto,
    titleChange: (String) -> Unit = {},
    detailsChange: (String) -> Unit = {}
) {
    // 添加标题
    TextInputCompose(modifier,
        remindDto.title,
        titleChange,
        textStyle = MaterialTheme.typography.headlineMedium,
        placeholderCompose = {
            Text(
                stringResource(Res.string.add_title),
                style = MaterialTheme.typography.headlineMedium
            )
        })
    HorizontalDivider()
    // 添加详情信息
    TextInputCompose(modifier,
        remindDto.details,
        detailsChange,
        singleLine = false,
        textStyle = MaterialTheme.typography.titleLarge,
        placeholderCompose = {
            Text(
                stringResource(Res.string.add_details), style = MaterialTheme.typography.titleLarge
            )
        },
        leadingIconCompose = {
            Icon(painter = painterResource(Res.drawable.notes), "info")

        })
    // HorizontalDivider()
}

