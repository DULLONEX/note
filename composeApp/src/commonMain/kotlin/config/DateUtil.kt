package config

import androidx.compose.material3.ExperimentalMaterial3Api
import isSameWeek
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun Long.toLocalDateTime(): LocalDateTime {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime
}

// 获取今天0.0.0.0
fun getTodayZero(): LocalDateTime {
    return LocalDateTime(getCurrentDateTime().date, LocalTime(0, 0, 0))
}

/**
 * 用于日期处理
 */
fun formatDateString(milliseconds: Long?): String {
    return milliseconds?.let {
        val instant = Instant.fromEpochMilliseconds(milliseconds)
        return@let instant.toLocalDateTime(TimeZone.currentSystemDefault()).formatToString()
    } ?: ""
}

fun convertLocalDateTime(milliseconds: Long?): LocalDateTime {
    return milliseconds?.let {
        val instant = Instant.fromEpochMilliseconds(milliseconds)
        return@let instant.toLocalDateTime(TimeZone.currentSystemDefault())
    } ?: getCurrentDateTime()
}


fun getCurrentDateTimeLong(): Long {
    val currentInstant = Clock.System.now()
    return currentInstant.toEpochMilliseconds()
}

fun getCurrentDateTime(): LocalDateTime {
    val currentInstant = Clock.System.now()
    val currentTimeZone = TimeZone.currentSystemDefault()
    return currentInstant.toLocalDateTime(currentTimeZone)
}

fun getCurrentDateTimeAfter(minute: Int): LocalDateTime {
    var currentInstant = Clock.System.now()
    currentInstant = currentInstant.plus(minute, DateTimeUnit.MINUTE)
    val currentTimeZone = TimeZone.currentSystemDefault()
    return currentInstant.toLocalDateTime(currentTimeZone)
}

@OptIn(ExperimentalMaterial3Api::class)
fun LocalDateTime.formatToString(): String {
    val year = this.yearString()
    val month = this.monthNumberString()
    val day = this.dayOfMonthString()
    return "$year/$month/$day"
}

fun LocalDateTime.formatWholeString(): String {
    val year = this.yearString()
    val month = this.monthNumberString()
    val day = this.dayOfMonthString()

    val hour = this.hour.toString().padStart(2, '0')
    val minute = this.minute.toString().padStart(2, '0')
    return "$year/$month/$day $hour:$minute"
}

fun LocalDateTime.formatTimeToString(): String {
    val hour = this.hour.toString().padStart(2, '0')
    val minute = this.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

fun getDayOfWeekName(date: LocalDate, language: String = "zh"): String {
    val dayOfWeek = date.dayOfWeek
    return when (language) {
        "zh" -> when (dayOfWeek) {
            DayOfWeek.MONDAY -> "星期一"
            DayOfWeek.TUESDAY -> "星期二"
            DayOfWeek.WEDNESDAY -> "星期三"
            DayOfWeek.THURSDAY -> "星期四"
            DayOfWeek.FRIDAY -> "星期五"
            DayOfWeek.SATURDAY -> "星期六"
            DayOfWeek.SUNDAY -> "星期天"
            else -> ""
        }

        "en" -> when (dayOfWeek) {
            DayOfWeek.MONDAY -> "Monday"
            DayOfWeek.TUESDAY -> "Tuesday"
            DayOfWeek.WEDNESDAY -> "Wednesday"
            DayOfWeek.THURSDAY -> "Thursday"
            DayOfWeek.FRIDAY -> "Friday"
            DayOfWeek.SATURDAY -> "Saturday"
            DayOfWeek.SUNDAY -> "Sunday"
            else -> ""
        }

        else -> "Unknown"
    }
}

fun LocalDateTime.yearString(): String {
    return this.year.toString().padStart(4, '0')
}

fun LocalDateTime.monthNumberString(): String {
    return this.monthNumber.toString().padStart(2, '0')
}

fun LocalDateTime.dayOfMonthString(): String {
    return this.dayOfMonth.toString().padStart(2, '0')
}

fun LocalDateTime.formatSimple(date: LocalDateTime, language: String = "zh"): String {
    val currentDateTime = getCurrentDateTime()
    val nowYear = currentDateTime.yearString()
    val nowMonth = currentDateTime.monthNumberString()
    val nowDay = currentDateTime.dayOfMonthString()

    val year = this.yearString()
    val month = this.monthNumberString()
    val day = this.dayOfMonthString()

    this.date

    val goalYear = date.yearString()
    val goalMonth = date.monthNumberString()
    val goalDay = date.dayOfMonthString()

    return when {
        isSameWeek(goalYear.toInt(),goalMonth.toInt(),goalDay.toInt(),year.toInt(),month.toInt(),day.toInt())

        -> "${
            getDayOfWeekName(
                this.date, language
            )
        } ${this.formatTimeToString()}~${date.formatTimeToString()}"

        nowYear == year && year == goalYear -> "${month}/${day} ${this.formatTimeToString()} ~ ${goalMonth}/${goalDay} ${date.formatTimeToString()}"
        else -> "${this.formatWholeString()} ~ ${date.formatWholeString()}"
    }
}

fun LocalDateTime.toLong(): Long {
    val instant = this.toInstant(TimeZone.currentSystemDefault())
    return instant.toEpochMilliseconds()
}

fun LocalDateTime.differDay(localDateTime: LocalDateTime): Long {
    val target = this.toInstant(TimeZone.currentSystemDefault())
    val compare = localDateTime.toInstant(TimeZone.currentSystemDefault())
    return target.minus(compare).inWholeDays
}

fun LocalDateTime.compare(localDateTime: LocalDateTime): Int {
    val target = this.toInstant(TimeZone.currentSystemDefault())
    val compare = localDateTime.toInstant(TimeZone.currentSystemDefault())
    return target.compareTo(compare)
}

fun LocalDateTime.differMinute(localDateTime: LocalDateTime): Long {
    val target = this.toInstant(TimeZone.currentSystemDefault())
    val compare =
        LocalDateTime(this.date, localDateTime.time).toInstant(TimeZone.currentSystemDefault())
    return target.minus(compare).inWholeMinutes
}

// 扩展函数用于修改小时
fun LocalDateTime.withHour(hour: Int): LocalDateTime {
    return LocalDateTime(
        this.year,
        this.monthNumber,
        this.dayOfMonth,
        hour,
        this.minute,
        this.second,
        this.nanosecond
    )
}

// 扩展函数用于修改分钟
fun LocalDateTime.addDay(day: Int): LocalDateTime {
    return LocalDateTime(
        this.date.plus(day, DateTimeUnit.DAY), this.time
    )
}


fun LocalDateTime.addTime(minute: Int): LocalDateTime {
    return LocalDateTime(
        this.date, LocalTime.fromSecondOfDay(this.time.toSecondOfDay() + minute * 60)
    )
}

fun LocalDateTime.withMinute(minute: Int): LocalDateTime {
    return LocalDateTime(
        this.year,
        this.monthNumber,
        this.dayOfMonth,
        this.hour,
        minute,
        this.second,
        this.nanosecond
    )
}

fun LocalDateTime.withHourAndMinute(hour: Int, minute: Int): LocalDateTime {
    return LocalDateTime(
        this.year, this.monthNumber, this.dayOfMonth, hour, minute, this.second, this.nanosecond
    )
}


