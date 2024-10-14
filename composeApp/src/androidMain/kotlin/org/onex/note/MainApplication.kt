package org.onex.note

import AndroidPlatform
import Platform
import android.app.Application
import android.content.Context
import androidx.navigation.NavHostController
import data.Database
import data.DatabaseDriverFactory
import data.service.AmountTypeService
import data.service.AmountTypeServiceImpl
import data.service.ChargeUpService
import data.service.ChargeUpServiceImpl
import data.service.RemindService
import data.service.RemindServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ui.viewmodel.SaveChargeUpViewModel

val appModule = module {
    single<Platform> { (controller: NavHostController,context:Context) -> AndroidPlatform(controller,context) }
    single<Database> {(databaseDriverFactory: DatabaseDriverFactory)-> Database(databaseDriverFactory) }
    single<RemindService> { RemindServiceImpl() }
    single<AmountTypeService> { AmountTypeServiceImpl() }
    single<ChargeUpService> { ChargeUpServiceImpl() }
}


class MainApplication : Application() {
    companion object {
        lateinit var instance: MainApplication
            private set
    }

    override fun onCreate() {

        super.onCreate()
        instance = this
        startKoin {
            androidContext(this@MainApplication)
            androidLogger()
            modules(appModule)
        }
    }
}