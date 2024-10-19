import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import kotlinx.coroutines.launch
import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.close
import note.composeapp.generated.resources.save
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ui.screen.chargeUp.AddChargeUpCompose
import ui.screen.chargeUp.ChargeUpComposeScreen
import ui.screen.remind.AddRemindScreenCompose
import ui.screen.remind.RemindScreen
import ui.theme.AppTheme
import ui.viewmodel.FileViewModel


@Composable
@Preview
fun App() {
    AppTheme {
        val navCompose = NavCompose()

        navCompose.MainNav()
    }
}


class NavCompose : KoinComponent {
    private val remindService: RemindService by inject()

    private val platform: Platform by inject()

    @Composable
    fun MainNav(
        navController: NavHostController = platform.navController,
    ) {
        val fileViewModel: FileViewModel = viewModel{ FileViewModel() }
        val addRemindScreenCompose = AddRemindScreenCompose()
        val addChargeUpCompose = AddChargeUpCompose()
        val chargeUpComposeScreen = ChargeUpComposeScreen()
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
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(animationSpec = tween(0)) },
            exitTransition = { fadeOut(animationSpec = tween(0)) }) {


            composable(route = Route.REMIND.route) {
                Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
                    BottomNavigationBar(selected = Route.REMIND)
                }) { innerPadding ->
                    RemindScreen(
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    )
                }
            }
            composable(route = Route.CHARGE_UP.route) {
                Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
                    BottomNavigationBar(selected = Route.CHARGE_UP)
                }) { innerPadding ->
                    chargeUpComposeScreen.ChargeUpScreen(
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    )
                }
            }
            composable(route = Route.REMIND_ADD.route) {
                Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
                    BottomNavigationBar(selected = Route.REMIND)
                }) { innerPadding ->
                    addRemindScreenCompose.AddRemindScreen(
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    )
                }
            }
            composable(route = Route.CHARGE_UP_ADD.route) {
                Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
                    BottomNavigationBar(selected = Route.CHARGE_UP)
                }) { innerPadding ->
                    addChargeUpCompose.AddChargeUpScreen(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        fileViewModel = fileViewModel
                    )
                }
            }
            composable(
                route = "${Route.CHARGE_UP_DETAIL.route}/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->

                val id: Long? = backStackEntry.arguments?.getLong("id")
                Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
                    BottomNavigationBar(selected = Route.CHARGE_UP)
                }) { innerPadding ->
                    addChargeUpCompose.AddChargeUpScreen(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        id = id,
                        fileViewModel = fileViewModel
                    )
                }
            }
            composable(Route.CAMERA.route) {
                // 在这个页面进行拍摄
                CameraShoot(Modifier, {
                    navController.navigateUp()
                }, fileViewModel)
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
        modifier: Modifier = Modifier,
        selected:Route = Route.REMIND
    ) {
        val items = returnLabelIcon()
        NavigationBottomBar(modifier, items,selected=selected)
    }

    @Composable
    fun NavigationBottomBar(
        modifier: Modifier = Modifier,
        items: List<LabelIcon>,
        navController: NavHostController = platform.navController,
        selected:Route = Route.REMIND
    ) {
        NavigationBar(
            modifier = modifier, tonalElevation = 0.dp
        ) {
            items.forEachIndexed { _, item ->
                NavigationBarItem(icon = {
                    Icon(item.icon, item.showText)
                }, label = { Text(item.showText) }, selected = item.id == selected.route, onClick = {
                    navController.navigate(item.id)
                })
            }
        }
    }

}
