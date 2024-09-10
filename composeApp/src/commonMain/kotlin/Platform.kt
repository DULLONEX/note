import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.navigation.NavHostController
import data.entiry.EventRemind
import data.entiry.FileData
import data.entiry.RemindDto

interface Platform {
    // 路由导航
    val navController: NavHostController
        @Composable get

    // 添加日历活动
    fun addCalendarEvent(remindDto: RemindDto): String

    fun queryCalendarEvents(): List<EventRemind>
    fun getCurrentLanguage(): String

    @Composable
    fun SelectImageCompose(
        fileDataList: List<FileData?>,
        addFile:(ImageBitmap)->Unit,
        delFile:(ImageBitmap)->Unit,
        content: @Composable (
            imageBitmapArray: List<ImageBitmap>, addPhoto: () -> Unit,delImageChange:(ImageBitmap)->Unit
        ) -> Unit,
    )

    fun saveFileImage(imageBitmap: ImageBitmap):String

    suspend fun downFileImage(filePath:String):ImageBitmap?
}

/**
 * 判断是否同一周
 * @return
 */
expect fun isSameWeek(year1: Int, month1: Int, day1: Int, year2: Int, month2: Int, day2: Int): Boolean
