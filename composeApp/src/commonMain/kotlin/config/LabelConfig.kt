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
    val id: String, val icon: Painter, val showText: String
)

// 用于设置一些标签
@Composable
fun returnLabelIcon(): List<LabelIcon> {
    return listOf(
        LabelIcon(
            Route.REMIND.route,
            painterResource(Res.drawable.event_upcoming),
            stringResource(Res.string.remind)
        ),
        LabelIcon(
            Route.CHARGE_UP.route,
            painterResource(Res.drawable.request_quote),
            stringResource(Res.string.charge_up)
        ),
    )
}

sealed class Route(val route: String) {
    data object REMIND : Route("remind")
    data object CHARGE_UP : Route("charge_up")
    data object REMIND_ADD : Route("remind_add")
    data object CHARGE_UP_ADD : Route("charge_up_add")
    data object CHARGE_UP_DETAIL : Route("charge_up_detail")

    companion object {
        fun fromRoute(route: String): Route {
            return when (route) {
                REMIND.route -> REMIND
                CHARGE_UP.route -> CHARGE_UP
                REMIND_ADD.route -> REMIND_ADD
                CHARGE_UP_ADD.route -> CHARGE_UP_ADD
                CHARGE_UP_DETAIL.route -> CHARGE_UP_DETAIL
                else -> throw IllegalArgumentException("Unknown route: $route")
            }
        }
    }
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

