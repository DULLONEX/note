package data

import com.onex.AppDatabase
import com.onex.note.RemindQueries
import config.getCurrentDateTimeLong
import config.toLong
import data.entiry.RemindDto

class Database(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
     val remindQueries = database.remindQueries
     val amountTypeQueries = database.amountTypeQueries
     val chargeUpQueries = database.chargeUpQueries
}

fun RemindQueries.insertRemind(eventId:String,remind: RemindDto){
    insertRemind(eventId,remind.title,remind.details,remind.startDate.toLong(),remind.endDate.toLong(),remind.alarm,remind.beforeMinutes.toLong())
}

fun RemindQueries.updateRemind(id:Long,remind: RemindDto){
    updateRemind(remind.title,remind.details,remind.startDate.toLong(),remind.endDate.toLong(),remind.alarm,remind.beforeMinutes.toLong(),id)
}

fun RemindQueries.syncRemindStatus(){
    syncRemindStatus(endDate = getCurrentDateTimeLong(), startDate = getCurrentDateTimeLong(), endDate_ = getCurrentDateTimeLong())

}