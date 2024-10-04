import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import config.getTodayZero
import config.toLocalDateTime
import config.toLong
import data.entiry.EventRemind
import data.entiry.FileData
import data.entiry.RemindDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.onex.note.accredit.checkAccreditPermission
import org.onex.note.accredit.getCalendarId
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class AndroidPlatform(private val vc: NavHostController, private val context: Context) : Platform {

    override val navController: NavHostController
        @Composable get() = vc

    override fun addCalendarEvent(remindDto: RemindDto): String {
        // 判断是否有权限
        if (!checkAccreditPermission(Manifest.permission.WRITE_CALENDAR, context)) {
            throw Exception("没有日历权限")
        }
        val contentResolver = context.contentResolver

        val calendarId = getCalendarId(context).id
        var eventId: Long? = 0
        // calendarId
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, remindDto.startDate.toLong())
            put(CalendarContract.Events.DTEND, remindDto.endDate.toLong())
            put(CalendarContract.Events.TITLE, remindDto.title)
            put(CalendarContract.Events.DESCRIPTION, remindDto.details)
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }

        val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        eventId = uri?.lastPathSegment?.toLong()

        // Optionally add a reminder
        if (eventId != null) {
            // 添加提醒
            val reminderValues = ContentValues().apply {
                put(CalendarContract.Reminders.MINUTES, remindDto.beforeMinutes)
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
            }
            contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
        }
        return eventId!!.toString();
    }


    override fun queryCalendarEvents(): List<EventRemind> {
        val systemRemindArray = mutableListOf<EventRemind>()
        val alarmEventIds = mutableListOf<String>()
        val contentResolver: ContentResolver = context.contentResolver

        // Define the URI for querying calendar events
        val uri: Uri = CalendarContract.Events.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.HAS_ALARM,
        )
        val selection =
            "((${CalendarContract.Calendars.OWNER_ACCOUNT} = ?) " + "AND (${CalendarContract.Events.DTSTART} >= ?) )"
        val selectionArgs: Array<String> =
            arrayOf(getCalendarId(context).ownerAccount, getTodayZero().toLong().toString())
        val cursor: Cursor? = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            val idIndex = it.getColumnIndex(CalendarContract.Events._ID)
            val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
            val startIndex = it.getColumnIndex(CalendarContract.Events.DTSTART)
            val endIndex = it.getColumnIndex(CalendarContract.Events.DTEND)
            val descriptionIndex = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)
            val alarmIndex = it.getColumnIndex(CalendarContract.Events.HAS_ALARM)

            // 遍历游标
            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val title = it.getString(titleIndex)
                val start = it.getLong(startIndex)
                val end = it.getLong(endIndex)
                val details = it.getString(descriptionIndex)
                val isAlarm = it.getString(alarmIndex) == "1"

                if (isAlarm) {
                    alarmEventIds.add(id.toString())
                }
                // 将时间戳转换为可读的日期/时间格式
                val startDate = start.toLocalDateTime()
                val endDate = end.toLocalDateTime()
                systemRemindArray.add(
                    EventRemind(
                        id.toString(), RemindDto(title, details, startDate, endDate, isAlarm)
                    )
                )
            }
        }

        if (alarmEventIds.isNotEmpty()) {
            val alarmMap = findAlarmEventArray(alarmEventIds)
            systemRemindArray.forEach {
                val beforeMinutes = alarmMap[it.eventId] ?: 0
                it.remindDto =
                    it.remindDto.copy(_beforeMinutes = beforeMinutes, alarm = beforeMinutes > 0)
            }
        }

        return systemRemindArray
    }

    private fun findAlarmEventArray(eventId: List<String>): Map<String, Int> {
        val map = mutableMapOf<String, Int>()

        val contentResolver: ContentResolver = context.contentResolver
        val remindersUri: Uri = CalendarContract.Reminders.CONTENT_URI

        val reminderProjection = arrayOf(
            CalendarContract.Reminders.MINUTES, CalendarContract.Reminders.EVENT_ID
        )

        val reminderSelection = eventId.joinToString(
            " OR ", "(", ")"
        ) { "(${CalendarContract.Reminders.EVENT_ID} = ?)" }


        val reminderCursor: Cursor? = contentResolver.query(
            remindersUri, reminderProjection, reminderSelection, eventId.toTypedArray(), null
        )

        // map 只保留第一个
        reminderCursor?.use {
            val minutesIndex = it.getColumnIndex(CalendarContract.Reminders.MINUTES)
            val eventIdIndex = it.getColumnIndex(CalendarContract.Reminders.EVENT_ID)

            while (it.moveToNext()) {
                val id = it.getLong(eventIdIndex)
                val minutesBefore = it.getInt(minutesIndex)
                if (map[id.toString()] == null) {
                    map[id.toString()] = minutesBefore
                }
            }
        }

        return map;
    }


    override fun getCurrentLanguage(): String {
        val configuration = context.resources.configuration
        return configuration.locales.get(0).language
    }

    @Composable
    override fun SelectImageCompose(
        fileDataList: List<FileData?>,
        addFile: (ImageBitmap) -> Unit,
        delFile: (ImageBitmap) -> Unit,
        content: @Composable (
            imageBitmapArray: List<ImageBitmap>, addPhoto: () -> Unit, delImageChange: (ImageBitmap) -> Unit
        ) -> Unit,
    ) {

        var imageUri by remember { mutableStateOf<Uri?>(null) }

        // 使用 snapshotFlow 将 fileDataList 的 imageBitmap 列表转换为 Flow
        val imageBitmapListState by snapshotFlow { fileDataList.map { it?.imageBitmap } }.collectAsState(
            mutableListOf()
        )


        val context = LocalContext.current

        // Launcher for selecting image
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                imageUri = it
                // Convert Uri to ImageBitmap
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                addFile(bitmap.asImageBitmap())
            }
        }

        content(imageBitmapListState.filterNotNull().toList(),
            { launcher.launch("image/*") },
            {
                delFile(it)
            })

    }

    override fun saveFileImage(imageBitmap: ImageBitmap): String {
        val bitmap = imageBitmap.asAndroidBitmap()
        val fileName = UUID.randomUUID().toString() + ".png"
        val file = File(context.getExternalFilesDir(null), fileName)
        println(file.toString())
        try {
            // 创建输出流
            val outputStream = FileOutputStream(file)
            outputStream.use {
                // 将 Bitmap 压缩并保存为指定格式
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return fileName
    }

    override suspend fun downFileImage(filePath: String): ImageBitmap? {
        val directory = context.getExternalFilesDir(null) // 例如，应用的外部存储目录

        // 读取本地文件中的图片
        return withContext(Dispatchers.IO) {
            val bitmap = BitmapFactory.decodeFile("${directory?.absolutePath}/$filePath")
            return@withContext bitmap?.asImageBitmap()
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
actual fun isSameWeek(
    year1: Int,
    month1: Int,
    day1: Int,
    year2: Int,
    month2: Int,
    day2: Int
): Boolean {
    val date1 = LocalDate.of(year1, month1, day1)
    val date2 = LocalDate.of(year2, month2, day2)
    val current = LocalDate.now()

    val weekFields = WeekFields.of(Locale.getDefault())
    val weekOfYear1 = date1.get(weekFields.weekOfWeekBasedYear())
    val weekOfYear2 = date2.get(weekFields.weekOfWeekBasedYear())
    val weekOfYearCurrent = current.get(weekFields.weekOfWeekBasedYear())
    val currentYear = current.year

    return weekOfYear1 == weekOfYear2
            && year1 == year2
            && weekOfYear1 == weekOfYearCurrent
            && year1 == currentYear

}

actual fun sumAmount(amount: List<String>): String {

    return amount.sumOf { BigDecimal(it).setScale(2, RoundingMode.UP) }.toPlainString()
}