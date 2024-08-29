package ui.screen.remind


import NavCompose
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import config.RemindStatus
import config.Route
import config.listTab
import kotlinx.coroutines.launch
import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.add_remind_info
import org.jetbrains.compose.resources.stringResource
import ui.MyNewIndicator
import ui.NoRippleInteractionSource
import ui.viewmodel.RemindViewModel
import data.entiry.ShowRemind

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemindScreen(
    modifier: Modifier = Modifier,
    viewModel: RemindViewModel = viewModel { RemindViewModel() }
) {
    val tabs = listTab.map { stringResource(it) }
    val coroutineScope = rememberCoroutineScope()
    var isSheet by remember { mutableStateOf(false) }
//    var id, status,title
    var remindId by remember { mutableStateOf(0L) }
    var remindStatus by remember { mutableStateOf(RemindStatus.ONGOING) }
    var remindTitle by remember { mutableStateOf("") }

    val pagerState = rememberPagerState(pageCount = {
        tabs.size
    }, initialPage = RemindStatus.ONGOING.value.toInt())

    val showRemind by viewModel.showRemind.collectAsState()

    val navCompose = NavCompose()
    Scaffold(modifier = modifier, floatingActionButton = {
        navCompose.FloatingAction(
            toRoute = Route.REMIND_ADD, description = stringResource(Res.string.add_remind_info)
        )
    }) {
        // 查询信息为主.
        Column(Modifier.fillMaxSize()) {
            TopSelectTab(Modifier, pagerState = pagerState, tabs = tabs, onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(
                        it, pageOffsetFraction = pagerState.currentPageOffsetFraction
                    )
                }
            })
            RemindHorizontalPager(modifier = Modifier,
                pagerState = pagerState,
                showRemind,
                viewModel::refresh,
                onClick = { id ->

                },
                onLongClick = { id, status, title ->
                    remindId = id
                    remindStatus = status
                    remindTitle = title
                    isSheet = !isSheet
                })
            // sheet
            AnimatedVisibility(isSheet) {
                BottomSheet(Modifier,
                    remindTitle,
                    remindId,
                    remindStatus,
                    aheadFinishClick = { id ->
                        coroutineScope.launch {
                            viewModel.remindStatusUpdate(id, RemindStatus.DONE)
                        }
                    },
                    aheadStartClick = { id ->
                        coroutineScope.launch {
                            viewModel.remindStatusUpdate(id, RemindStatus.ONGOING)
                        }
                    },
                    delClick = { id ->
                        coroutineScope.launch {
                            viewModel.delById(id)
                        }
                    },
                    showChange = { isSheet = !isSheet })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    title: String = "",
    id: Long = 0L,
    status: RemindStatus = RemindStatus.ONGOING,
    aheadStartClick: (Long) -> Unit = {},
    aheadFinishClick: (Long) -> Unit = {},
    delClick: (Long) -> Unit = {},
    showChange: () -> Unit = {}
) {
    ModalBottomSheet(modifier = modifier.heightIn(min = 200.dp),
        onDismissRequest = { showChange() }) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题
            Text(title, style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(50.dp)) {
                val aheadStart = @Composable {
                    TextButton({
                        aheadStartClick(id)
                        showChange()
                    }) {
                        Text("提前开始")
                    }
                }
                val aheadFinish = @Composable {
                    TextButton({
                        aheadFinishClick(id)
                        showChange()
                    }) {
                        Text("提前完成")
                    }
                }

                when (status) {
                    config.RemindStatus.NOT_STATE -> {
                        aheadStart()
                        aheadFinish()
                    }

                    config.RemindStatus.ONGOING -> {
                        aheadFinish()
                    }

                    config.RemindStatus.DONE -> {

                    }
                }
            }

            FilledTonalButton({
                delClick(id)
                showChange()
            }) {
                Text("删除")
            }
        }
    }

}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun RemindHorizontalPager(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    showRemindList: Map<RemindStatus, List<ShowRemind>>,
    refresh: suspend () -> Unit = {},
    onLongClick: (id: Long, status: RemindStatus, title: String) -> Unit = { _, _, _ -> },
    onClick: (id: Long) -> Unit = {}

) {
    var refreshState by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // refresh 并没有什么用,因为flow自带刷新数据.写了懒得去删除了
    val pullRefreshState = rememberPullRefreshState(refreshing = refreshState, {
        scope.launch {
            refreshState = !refreshState
            refresh()
            refreshState = !refreshState
        }
    }, refreshThreshold = 25.dp)
    val topDp = 60.dp
    val minDp = 0.dp
    // 头部动画位置
    val animatedOffset by animateDpAsState(
        targetValue = when {
            pullRefreshState.progress.toInt() == 0 && refreshState -> topDp
            else -> {
                val tDp = minDp + (pullRefreshState.progress * (topDp - minDp))
                tDp.coerceAtMost(topDp)
            }
        }, animationSpec = tween(durationMillis = 300) // Adjust duration for smoothness
    )

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize().pullRefresh(pullRefreshState),
        verticalAlignment = Alignment.Top,
    ) { page ->
        val remindStatus = RemindStatus.fromValue(page.toLong())
        var currentPageReminds = showRemindList[remindStatus]
        if (remindStatus == RemindStatus.DONE) {
            // 完成需要倒序 才能正确的显示出来
            currentPageReminds = currentPageReminds?.reversed()
        }
        Box {
            LazyColumn(
                modifier = Modifier.fillMaxSize().offset(y = animatedOffset),
                contentPadding = PaddingValues(horizontal = 5.dp, vertical = 5.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (currentPageReminds != null) {
                    items(currentPageReminds, key = {it.id}) { item ->
                        RemindItem(
                            Modifier.combinedClickable(onClick = {
                                onClick(item.id)
                            }, onLongClick = {
                                onLongClick(item.id, item.status, item.title)
                            }), item.title, item.time, item.details
                        )
                    }
                }
            }
            PullRefreshIndicator(
                refreshState, pullRefreshState, Modifier.align(Alignment.TopCenter), scale = true
            )
        }
    }
}


@Composable
fun RemindItem(
    modifier: Modifier = Modifier,
    title: String = "",
    time: String = "",
    details: String = "",
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth().heightIn(min = 100.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(15.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(text = time, style = MaterialTheme.typography.titleMedium)
            }
            Text(text = details.trimIndent(), style = MaterialTheme.typography.titleSmall)
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopSelectTab(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    tabs: List<String>,
    onClick: (Int) -> Unit = {}
) {
    val myIndicator = @Composable { tabPositions: List<TabPosition> ->
        if (pagerState.currentPage < tabPositions.size) {
            MyNewIndicator(
                Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
            )
        }
    }
    TabRow(selectedTabIndex = pagerState.currentPage,
        indicator = myIndicator,
        modifier = modifier,
        contentColor = MaterialTheme.colorScheme.outline,
        divider = {
            HorizontalDivider(
                thickness = 3.dp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.01f)
            )
        }) {
        tabs.forEachIndexed { index, title ->
            Tab(
                text = { Text(title) },
                selected = pagerState.currentPage == index,
                onClick = { onClick(index) },
                modifier = Modifier.zIndex(2f),
                interactionSource = NoRippleInteractionSource()
            )
        }
    }
}