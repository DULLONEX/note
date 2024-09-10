import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import config.LabelIcon
import config.Route
import config.returnLabelIcon
import data.service.RemindService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.close
import note.composeapp.generated.resources.save
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ui.screen.chargeUp.AddChargeUpCompose
import ui.screen.chargeUp.ChargeUpCompose
import ui.screen.remind.AddRemindScreenCompose
import ui.screen.remind.RemindScreen
import ui.theme.AppTheme


@Composable
@Preview
fun App() {
    AppTheme {
        val navCompose = NavCompose()
        Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
            navCompose.BottomNavigationBar()
        }) { innerPadding ->
            navCompose.MainNav(innerPadding = innerPadding)
        }
    }
}


class NavCompose : KoinComponent {
    private val remindService: RemindService by inject()

    private val platform: Platform by inject()

    @Composable
    fun MainNav(
        navController: NavHostController = platform.navController, innerPadding: PaddingValues
    ) {
        val addRemindScreenCompose = AddRemindScreenCompose()
        val addChargeUpCompose = AddChargeUpCompose()
        val chargeUpCompose = ChargeUpCompose()
        var isActive by remember { mutableStateOf(true) }

        rememberCoroutineScope().launch {
            while (isActive) {
                remindService.initialLoad()
                delay(15000)
            }
        }
        // 在合适的地方取消协程
        DisposableEffect(Unit) {
            onDispose {
                isActive = false
            }
        }
        NavHost(navController = navController,
            startDestination = Route.REMIND.route,
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(0)) },
            exitTransition = { fadeOut(animationSpec = tween(0)) }) {


            composable(route = Route.REMIND.route) {
                RemindScreen(modifier = Modifier.size(100.dp))
            }
            composable(route = Route.CHARGE_UP.route) {
                chargeUpCompose.ChargeUpScreen(modifier = Modifier.size(100.dp))
            }
            composable(route = Route.REMIND_ADD.route) {
                addRemindScreenCompose.AddRemindScreen(modifier = Modifier.size(100.dp))
            }
            composable(route = Route.CHARGE_UP_ADD.route) {
                addChargeUpCompose.AddChargeUpScreen(modifier = Modifier.size(100.dp))
            }
            composable(
                route = "${Route.CHARGE_UP_DETAIL.route}/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id: Long? = backStackEntry.arguments?.getLong("id")
                addChargeUpCompose.AddChargeUpScreen(modifier = Modifier.size(100.dp), id = id)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopAppBarCompose(
        modifier: Modifier = Modifier,
        navController: NavHostController = platform.navController,
        onActionClick: () -> Unit = {}
    ) {
        TopAppBar(modifier = modifier, title = {}, navigationIcon = {
            IconButton(onClick = {
                navController.navigateUp()
            }) {
                Icon(Icons.Default.Close, stringResource(Res.string.close))
            }
        }, actions = {
            Button({
                onActionClick()
            }) {
                Text(stringResource(Res.string.save))
            }
        })
    }

    @Composable
    fun FloatingAction(
        modifier: Modifier = Modifier,
        navController: NavHostController = platform.navController,
        description: String,
        toRoute: Route = Route.REMIND
    ) {
        FloatingActionButtonCompose(modifier, description) {
            navController.navigate(toRoute.route)
        }
    }


    @Composable
    fun FloatingActionButtonCompose(
        modifier: Modifier = Modifier, description: String, onClick: () -> Unit
    ) {
        FloatingActionButton(
            onClick = {
                onClick()
            },
            modifier = modifier.padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, description)
        }
    }


    @Composable
    fun BottomNavigationBar(
        modifier: Modifier = Modifier
    ) {
        val items = returnLabelIcon()
        NavigationBottomBar(modifier, items)
    }

    @Composable
    fun NavigationBottomBar(
        modifier: Modifier = Modifier,
        items: List<LabelIcon>,
        navController: NavHostController = platform.navController
    ) {
        var selectedItem by rememberSaveable { mutableIntStateOf(0) }

        NavigationBar(
            modifier = modifier, tonalElevation = 0.dp
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedItem == index
                NavigationBarItem(icon = {
                    Icon(item.icon, item.showText)
                }, label = { Text(item.showText) }, selected = isSelected, onClick = {
                    selectedItem = index
                    navController.navigate(item.id)
                })
            }
        }
    }

}
