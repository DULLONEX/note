package data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.onex.spacetutorial.cache.AppDatabase

class IOSDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        // deleteDatabase("note.db")
        return NativeSqliteDriver(AppDatabase.Schema, "note.db")
    }
}