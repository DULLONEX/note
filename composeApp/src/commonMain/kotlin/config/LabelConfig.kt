package config

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.datetime.Clock
import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.charge_up
import note.composeapp.generated.resources.event_upcoming
import note.composeapp.generated.resources.remind
import note.composeapp.generated.resources.request_quote
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

data class LabelIcon(
    val id: String,
    val icon: Painter,
    val showText: String
)

// 用于设置一些标签
@Composable
fun returnLabelIcon(): List<LabelIcon> {
    return listOf(
        LabelIcon(Route.REMIND.name, painterResource(Res.drawable.event_upcoming), stringResource(Res.string.remind)),
        LabelIcon(Route.CHARGE_UP.name, painterResource(Res.drawable.request_quote), stringResource(Res.string.charge_up)),
    )
}

enum class Route(s: String) {
    REMIND("remind"), CHARGE_UP("charge_up"),REMIND_ADD("remind_add"),CHARGE_UP_ADD("charge_up_add")
}


class Timer {
    private var startTime: Long = 0

    fun start() {
        startTime = Clock.System.now().toEpochMilliseconds()
    }

    fun stop(): Long {
        return Clock.System.now().toEpochMilliseconds() - startTime
    }
}

