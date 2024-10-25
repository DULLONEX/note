package ui.screen.chargeUp

import NavCompose
import Platform
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import config.Route
import config.formatToString
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

class ChargeUpComposeScreen : KoinComponent {
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
    var showMap by rememberSaveable(chargeUpMapList) {
        mutableStateOf(
            chargeUpMapList.keys.asSequence().map { it.currentDate to true }.toMap()
        )
    }

    LazyColumn(modifier.padding(horizontal = 4.dp)) {
        chargeUpMapList.forEach { it ->

            stickyHeader {
                StickyHeaderCompose(Modifier.zIndex(1f).clickable {
                    showMap = showMap.toMutableMap().apply {
                        this[it.key.currentDate] = !this[it.key.currentDate]!!
                    }
                }, it.key.currentDate, it.key.sumAmount)
            }

            items(it.value.size, key = { index -> it.value[index].id!! }) { index ->
                AnimatedVisibility(
                    modifier = Modifier.padding(bottom = 16.dp),
                    visible = showMap[it.key.currentDate]!!,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = {
                            -it / 2 * (index + 1)
                        },
                        animationSpec = tween(100)
                    ), exit = slideOutVertically(
                        targetOffsetY = {
                            -it / 2 * (index + 1)
                        },
                        animationSpec = tween(50)
                    ) + fadeOut(animationSpec = tween(25))
                ) {
                    SlippableChargeUp(
                        chargeUpDto = it.value[index],
                        currentSlippingItem = currentSlippingItem,
                        delClick = delClick,
                        goDetail = goDetail
                    )
                }


            }


            item {
                HorizontalDivider()
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
        Text("-$sumAmount¥", style = MaterialTheme.typography.titleMedium)
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
            snapAnimationSpec = tween(),
            decayAnimationSpec = exponentialDecay(),
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
        ChargeUpItem(modifier.fillMaxHeight().heightIn(max = 100.dp), chargeUpDto, goDetail)
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
    modifier: Modifier = Modifier, chargeUpDto: ChargeUpDto, goDetail: (Long) -> Unit = {}
) {
    val amountTypeDto = chargeUpDto.amountType
    ElevatedCard(modifier.heightIn(min = 100.dp).clickable {
        goDetail(chargeUpDto.id!!)
    }, shape = RectangleShape) {
        Column(Modifier.padding(16.dp)) {
            // 日期
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    modifier = Modifier.weight(0.4f),
                    text = chargeUpDto.fillTime.formatToString(),
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(
                    modifier = Modifier.weight(0.6f),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (amountTypeDto.whetherSystem) stringResource(
                            getStringResource(
                                amountTypeDto.message
                            )
                        ) else amountTypeDto.message,
                        Modifier.alignByBaseline().weight(0.6f),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.End
                    )
                    Text(
                        "-${chargeUpDto.amount}¥",
                        Modifier.alignByBaseline().weight(0.4f),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.End
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                chargeUpDto.content,
                Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
            )
        }


    }


}

