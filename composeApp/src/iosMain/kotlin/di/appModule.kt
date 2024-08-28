import androidx.navigation.NavHostController
import data.Database
import data.DatabaseDriverFactory
import data.service.RemindService
import data.service.RemindServiceImpl
import org.koin.dsl.module

val iosModule = module {

    single<Platform> {
        (controller: NavHostController) -> iOSPlatform(controller)
    }
    single<Database> { (databaseDriverFactory: DatabaseDriverFactory)-> Database(databaseDriverFactory) }
    single<RemindService> { RemindServiceImpl() }

}