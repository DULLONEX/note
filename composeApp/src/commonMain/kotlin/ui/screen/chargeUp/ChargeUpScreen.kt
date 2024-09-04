package ui.screen.chargeUp

import NavCompose
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import config.Route
import config.getStringResource
import data.entiry.ChargeUpDto
import data.entiry.MonthSumCharge
import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.add_charge_up_info
import org.jetbrains.compose.resources.stringResource
import ui.viewmodel.ChargeUpViewModel

@Composable
fun ChargeUpScreen(
    modifier: Modifier = Modifier,
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
        ChargeUpCompose(Modifier,chargeUpMap)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChargeUpCompose(
    modifier: Modifier = Modifier,
    chargeUpMapList: Map<MonthSumCharge, List<ChargeUpDto>> = hashMapOf()
) {
    LazyColumn(modifier.padding(horizontal = 4.dp)) {
        chargeUpMapList.forEach { it ->
            stickyHeader {
                StickyHeaderCompose(Modifier, it.key.currentDate, it.key.sumAmount)
            }
            items(it.value) { item ->
                ChargeUpItem(
                    chargeUpDto = item
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StickyHeaderCompose(
    modifier: Modifier = Modifier,
    date: String = "",
    sumAmount: String = ""
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

@Composable
fun ChargeUpItem(
    modifier: Modifier = Modifier,
    chargeUpDto: ChargeUpDto
) {
    val amountTypeDto = chargeUpDto.amountType
    ElevatedCard(modifier.fillMaxWidth().heightIn(min = 100.dp)) {
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
                        Modifier.alignByBaseline(), style = MaterialTheme.typography.labelLarge
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

