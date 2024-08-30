package org.onex.note.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.onex.AppDatabase
import data.DatabaseDriverFactory

class AndroidDatabaseDriverFactory(private val context: Context) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
         //context.deleteDatabase("note.db")
        return AndroidSqliteDriver(AppDatabase.Schema, context, "note.db")
    }

}