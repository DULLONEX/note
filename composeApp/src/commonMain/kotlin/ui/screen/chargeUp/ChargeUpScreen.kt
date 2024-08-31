package ui.screen.chargeUp

import NavCompose
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import config.Route
import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.add_charge_up_info
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChargeUpScreen(
    modifier: Modifier = Modifier

) {
    val navCompose = NavCompose()

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

    }

}