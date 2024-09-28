import androidx.compose.ui.window.ComposeUIViewController
import androidx.navigation.compose.rememberNavController
import data.Database
import data.IOSDatabaseDriverFactory
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf
import platform.UIKit.UIViewController


fun ComposeEntryPointWithUIViewController(
    createUIViewController: () -> UIViewController
): UIViewController = ComposeUIViewController {
    // 申请权限
    requestCalendarAccess { granted, error ->

    }

    val navController = rememberNavController()
    startKoin {
        modules(iosModule)
    }.also {
        it.koin.get<Platform> { parametersOf(navController) }
        it.koin.get<Database> { parametersOf(IOSDatabaseDriverFactory()) }
    }

    App()
}