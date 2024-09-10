package ui.screen.chargeUp

import NavCompose
import Platform
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import config.Route
import config.getStringResource
import data.entiry.ChargeUpDto
import data.entiry.MonthSumCharge
import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.add_charge_up_info
import org.jetbrains.compose.resources.stringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ui.DelAction
import ui.DeleteConfirmationDialog
import ui.DragAnchors
import ui.DraggableItem
import ui.viewmodel.ChargeUpViewModel
import kotlin.math.roundToInt

class ChargeUpCompose : KoinComponent {
    private val platform: Platform by inject()

    @Composable
    fun ChargeUpScreen(
        modifier: Modifier = Modifier,
        navController: NavHostController = platform.navController,
        viewModel: ChargeUpViewModel = viewModel { ChargeUpViewModel() },

        ) {
        val navCompose = NavCompose()
        val chargeUpMap by viewModel.chargeUpMap.collectAsState()
        Scaffold(modifier, floatingActionButton = {
            navCompose.FloatingAction(
                toRoute = Route.CHARGE_UP_ADD,
                description = stringResource(Res.string.add_charge_up_info)
            )
        }) {
            /**
             * 类似与常见的那种账单页面
             * 根据时间排序
             * 上面tap：年-月 总金额
             * 下面一条条账单
             */
            ChargeUpCompose(Modifier, chargeUpMap, delClick = viewModel::delById, goDetail = {
                navController.navigate("${Route.CHARGE_UP_DETAIL.route}/$it")
            })
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChargeUpCompose(
    modifier: Modifier = Modifier,
    chargeUpMapList: Map<MonthSumCharge, List<ChargeUpDto>> = hashMapOf(),
    delClick: (Long) -> Unit = {},
    goDetail: (Long) -> Unit = {}
) {
    val currentSlippingItem = remember { mutableStateOf<Long?>(null) }

    LazyColumn(modifier.padding(horizontal = 4.dp)) {
        chargeUpMapList.forEach { it ->
            stickyHeader {
                StickyHeaderCompose(Modifier, it.key.currentDate, it.key.sumAmount)
            }
            items(it.value, key = { it.id!! }) { item ->
                SlippableChargeUp(
                    chargeUpDto = item, currentSlippingItem = currentSlippingItem,
                    delClick = delClick, goDetail = goDetail
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StickyHeaderCompose(
    modifier: Modifier = Modifier, date: String = "", sumAmount: String = ""
) {
    Row(
        modifier.fillMaxWidth().background(MaterialTheme.colorScheme.onPrimary)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(date, style = MaterialTheme.typography.titleLarge)
        Text("¥: -$sumAmount", style = MaterialTheme.typography.titleMedium)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SlippableChargeUp(
    modifier: Modifier = Modifier,
    chargeUpDto: ChargeUpDto,
    currentSlippingItem: MutableState<Long?>,
    delClick: (Long) -> Unit = {},
    goDetail: (Long) -> Unit = {}
) {
    var isShowAlter by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    val defaultActionSize = 80.dp

    val actionSizePx = with(density) { defaultActionSize.toPx() }
    val endActionSizePx = with(density) { (defaultActionSize).toPx() }

    val state = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Center,
            anchors = DraggableAnchors {
                //  DragAnchors.Start at actionSizePx
                DragAnchors.Center at 0f
                DragAnchors.End at -endActionSizePx
            },
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            animationSpec = tween(),
        )
    }


    LaunchedEffect(state.currentValue) {
        if (state.currentValue == DragAnchors.End) {
            currentSlippingItem.value = chargeUpDto.id
        }
    }
    LaunchedEffect(currentSlippingItem.value) {
        currentSlippingItem.value?.apply {
            if (currentSlippingItem.value != chargeUpDto.id) {
                state.animateTo(DragAnchors.Center)
            }
        }
    }

    DraggableItem(state = state, content = {
        ChargeUpItem(modifier.fillMaxHeight(), chargeUpDto, goDetail)
    }, endAction = {
        DelAction(Modifier.fillMaxHeight().align(Alignment.CenterEnd)
            .background(MaterialTheme.colorScheme.error).width(defaultActionSize).fillMaxHeight()
            .heightIn(min = 100.dp).offset {
                IntOffset(
                    ((state.requireOffset() + endActionSizePx)).roundToInt(), 0
                )
            }.clickable { isShowAlter = !isShowAlter })
    })

    DeleteConfirmationDialog(showDialog = isShowAlter, onConfirmDelete = {
        delClick(chargeUpDto.id!!)
        isShowAlter = !isShowAlter
    }, onDismissRequest = {
        isShowAlter = !isShowAlter
    })
}


@Composable
fun ChargeUpItem(
    modifier: Modifier = Modifier, chargeUpDto: ChargeUpDto,
    goDetail: (Long) -> Unit = {}
) {
    val amountTypeDto = chargeUpDto.amountType
    ElevatedCard(modifier.heightIn(min = 100.dp).clickable {
        goDetail(chargeUpDto.id!!)
    }, shape = RectangleShape) {
        Column(Modifier.padding(16.dp)) {
            // 日期
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(chargeUpDto.createTime, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        if (amountTypeDto.whetherSystem) stringResource(
                            getStringResource(
                                amountTypeDto.message
                            )
                        ) else amountTypeDto.message,
                        Modifier.alignByBaseline(),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "¥: -${chargeUpDto.amount}",
                        Modifier.alignByBaseline(),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                chargeUpDto.content,
                Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }


    }


}

