package data.service

import Platform
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.onex.note.Remind
import config.RemindStatus
import config.formatSimple
import config.getCurrentDateTime
import config.getTodayZero
import config.toLocalDateTime
import config.toLong
import data.Database
import data.entiry.RemindDto
import data.insertRemind
import data.syncRemindStatus
import data.updateRemind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ui.viewmodel.ShowRemind

interface RemindService {
    fun saveRemind(remind: RemindDto)

    /**
     * 初始化加载 用于同步已经更新
     */
    suspend fun initialLoad()

    suspend fun findRemindAll(language: String): Flow<Map<RemindStatus, List<ShowRemind>>>

    suspend fun updateRemindStatus(id: Long, status: RemindStatus, start: Long, end: Long)

    suspend fun updateRemind(id: Long, remind: RemindDto)

    suspend fun delRemindById(id: Long)

    suspend fun findRemindById(id: Long): Remind

}

class RemindServiceImpl : RemindService, KoinComponent {
    private val database: Database by inject()
    val platform: Platform by inject()

    override fun saveRemind(remind: RemindDto) {
        val eventId = platform.addCalendarEvent(remind)
        database.remindQueries.insertRemind(eventId, remind)
    }

    override suspend fun initialLoad() = runBlocking(context = Dispatchers.IO) {
        /*
          需要操作步骤
          1.通过eventID 判断是否添加
          2.查询db中今日之后的数据判断系统是否存在进行删除操作
          3.通过以添加的数据进行数据同步更新
         */

        // 获取到今后的日历活动信息
        val systemCalendarEvents = async { platform.queryCalendarEvents() }

        val dbEvent = async {
            database.remindQueries.selectRemindsAfterStartDate(getTodayZero().toLong()).executeAsList()
        }
        val queryCalendarEvents = systemCalendarEvents.await()
        val dbRemindArray = dbEvent.await()
        val eventIdSet = queryCalendarEvents.map { it.eventId }.toSet()
        val tagMap = dbRemindArray.associateBy { it.tagId }

        database.remindQueries.transaction {
            queryCalendarEvents.forEach {
                if (tagMap[it.eventId] == null) {
                    // add
                    database.remindQueries.insertRemind(it.eventId, it.remindDto)
                } else {
                    // update
                    // database.dbQuery.updateRemind(tagMap[it.eventId]!!.id, it.remindDto)
                }
            }
        }
        database.remindQueries.transaction {
            dbRemindArray.filter { remind ->
                remind.tagId !in eventIdSet
            }.forEach {
                database.remindQueries.delRemind(it.id)
            }
        }
        // 4.状态进行修正
        database.remindQueries.transaction {
            database.remindQueries.syncRemindStatus()
        }

        println("initialLoad ok ${getCurrentDateTime()}")
    }

    override suspend fun findRemindAll(language: String): Flow<Map<RemindStatus, List<ShowRemind>>> {
        val map =
            database.remindQueries.selectAllReminds().asFlow().mapToList(Dispatchers.IO).map { array ->
                // time 处理
                array.map { remind ->
                    ShowRemind(
                        remind.id,
                        remind.tagId,
                        remind.title,
                        remind.startDate.toLocalDateTime().formatSimple(
                            remind.endDate.toLocalDateTime(), language
                        ),
                        remind.details,
                        status = RemindStatus.fromValue(remind.proceedStatus)
                    )
                }.groupBy { it.status }.toMutableMap()
            }

//        if (map[RemindStatus.DONE] != null) {
//            map[RemindStatus.DONE] = map[RemindStatus.DONE]!!.reversed()
//        }
        // 进行排序
        return map
    }

    override suspend fun updateRemindStatus(
        id: Long, status: RemindStatus, start: Long, end: Long
    ) {
        database.remindQueries.updateRemindStatus(status.value, start, end, id)
    }

    override suspend fun updateRemind(id: Long, remind: RemindDto) {
        database.remindQueries.updateRemind(id, remind)
    }

    override suspend fun delRemindById(id: Long) {
        database.remindQueries.delRemind(id)
    }

    override suspend fun findRemindById(id: Long): Remind {
        return database.remindQueries.selectRemindById(id).executeAsOne()
    }


}