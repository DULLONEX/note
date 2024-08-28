package data

import com.onex.spacetutorial.cache.AppDatabase
import com.onex.spacetutorial.cache.AppDatabaseQueries
import config.getCurrentDateTimeLong
import config.toLong
import data.entiry.RemindDto

class Database(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    public val dbQuery = database.appDatabaseQueries
}

fun AppDatabaseQueries.insertRemind(eventId:String,remind: RemindDto){
    insertRemind(eventId,remind.title,remind.details,remind.startDate.toLong(),remind.endDate.toLong(),remind.alarm,remind.beforeMinutes.toLong())
}

fun AppDatabaseQueries.updateRemind(id:Long,remind: RemindDto){
    updateRemind(remind.title,remind.details,remind.startDate.toLong(),remind.endDate.toLong(),remind.alarm,remind.beforeMinutes.toLong(),id)
}

fun AppDatabaseQueries.syncRemindStatus(){
    syncRemindStatus(endDate = getCurrentDateTimeLong(), startDate = getCurrentDateTimeLong(), endDate_ = getCurrentDateTimeLong())

}