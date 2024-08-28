package org.onex.note

import AndroidPlatform
import Platform
import android.app.Application
import android.content.Context
import androidx.navigation.NavHostController
import data.Database
import data.DatabaseDriverFactory
import data.service.RemindService
import data.service.RemindServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {
    single<Platform> { (controller: NavHostController,context:Context) -> AndroidPlatform(controller,context) }
    single<Database> {(databaseDriverFactory: DatabaseDriverFactory)-> Database(databaseDriverFactory) }
    single<RemindService> { RemindServiceImpl() }
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