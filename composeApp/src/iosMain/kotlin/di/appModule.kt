import androidx.navigation.NavHostController
import data.Database
import data.DatabaseDriverFactory
import data.service.AmountTypeService
import data.service.AmountTypeServiceImpl
import data.service.ChargeUpService
import data.service.ChargeUpServiceImpl
import data.service.RemindService
import data.service.RemindServiceImpl
import org.koin.dsl.module

val iosModule = module {

    single<Platform> {
        (controller: NavHostController) -> iOSPlatform(controller)
    }
    single<Database> { (databaseDriverFactory: DatabaseDriverFactory)-> Database(databaseDriverFactory) }
    single<RemindService> { RemindServiceImpl() }
    single<AmountTypeService> { AmountTypeServiceImpl() }
    single<ChargeUpService> { ChargeUpServiceImpl() }


}