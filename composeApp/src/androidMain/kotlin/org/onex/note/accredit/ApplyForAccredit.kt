package org.onex.note.accredit

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import org.onex.note.R

val accreditList = listOf(
    Manifest.permission.WRITE_CALENDAR,
    Manifest.permission.READ_CALENDAR,
    Manifest.permission.CAMERA,
)


fun showPermissionSettingsDialog(context: Context) {
    AlertDialog.Builder(context)
        .setTitle(context.getString(R.string.permission_denied_title))
        .setMessage(context.getString(R.string.permission_denied_message))
        .setPositiveButton(context.getString(R.string.go_to_settings)) { _, _ ->
            openAppSettings(context)
        }
        .setNegativeButton(context.getString(R.string.cancel), null)
        .show()
}

private fun openAppSettings(context: Context) {
    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = "package:${context.packageName}".toUri()
    startActivity(context, intent, null)
}

/**
 * false 无这个权限
 * true 已经获得这个权限
 *
 * @param accredit  Manifest.permission.xxx
// * @param context
 * @return
 */
fun checkAccreditPermission(accredit: String, context: Context): Boolean {
    if (ContextCompat.checkSelfPermission(
            context,
            accredit
        ) == PermissionChecker.PERMISSION_DENIED
    ) {
        showPermissionSettingsDialog(context)
    }

    return ContextCompat.checkSelfPermission(
        context,
        accredit
    ) == PermissionChecker.PERMISSION_GRANTED
}

fun getCalendarId(context: Context):CalendarUser{
    val calendarsUri = CalendarContract.Calendars.CONTENT_URI
    val projection = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.OWNER_ACCOUNT
    )
    val contentResolver = context.contentResolver
    val cursor = contentResolver.query(calendarsUri, projection, null, null, null)
    var calendarId: Long? = null
    var ownerAccount:String? = null
    cursor?.use {
        while (it.moveToNext()){
            calendarId =
                cursor.getLong(it.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
            ownerAccount =
                cursor.getString(it.getColumnIndexOrThrow(CalendarContract.Calendars.OWNER_ACCOUNT))
        }

    }
    return CalendarUser(calendarId!!,ownerAccount!!)
}

data class CalendarUser(
    val id: Long,
    val ownerAccount: String

)