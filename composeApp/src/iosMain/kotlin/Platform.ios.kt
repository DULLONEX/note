import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.navigation.NavHostController
import config.getTodayZero
import config.toLong
import data.entiry.EventRemind
import data.entiry.FileData
import data.entiry.RemindDto
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGBitmapInfo
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGImageAlphaInfo
import platform.EventKit.EKAlarm
import platform.EventKit.EKCalendar
import platform.EventKit.EKCalendarType
import platform.EventKit.EKEntityType
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKit.EKSpan
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitWeekOfYear
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSLocale
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.URLByAppendingPathComponent
import platform.Foundation.currentLocale
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.languageCode
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToURL
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
fun ImageBitmap.toUIImage(): UIImage {
    val width = this.width
    val height = this.height
    val buffer = IntArray(width * height)

    // Read pixels into buffer
    this.readPixels(buffer)

    // Convert the buffer (ARGB) into a byte array (RGBA)
    val rgbaBuffer = ByteArray(buffer.size * 4)
    buffer.forEachIndexed { index, argb ->
        val argbColor = argb
        rgbaBuffer[index * 4 + 0] = (argbColor shr 16 and 0xFF).toByte()  // R
        rgbaBuffer[index * 4 + 1] = (argbColor shr 8 and 0xFF).toByte()   // G
        rgbaBuffer[index * 4 + 2] = (argbColor and 0xFF).toByte()         // B
        rgbaBuffer[index * 4 + 3] = (argbColor shr 24 and 0xFF).toByte()  // A
    }

    return memScoped {
        // Create color space
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        // Create context
        rgbaBuffer.usePinned { pinnedBuffer ->
            val context = CGBitmapContextCreate(
                data = pinnedBuffer.addressOf(0),
                width = width.toULong(),
                height = height.toULong(),
                bitsPerComponent = 8u,
                bytesPerRow = (4 * width).toULong(),
                space = colorSpace,
                bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
            )

            // Create CGImage from context
            val cgImage = CGBitmapContextCreateImage(context)

            // Return UIImage created from CGImage
            UIImage.imageWithCGImage(cgImage)
        }
    }
}


fun LocalDateTime.toNSDate(): NSDate {
    val calendar = NSCalendar.currentCalendar

    val components = NSDateComponents().apply {
        year = this@toNSDate.year.toLong()
        month = this@toNSDate.monthNumber.toLong()
        day = this@toNSDate.dayOfMonth.toLong()
        hour = this@toNSDate.hour.toLong()
        minute = this@toNSDate.minute.toLong()
        second = this@toNSDate.second.toLong()
    }
    return calendar.dateFromComponents(components) ?: NSDate()
}

fun NSDate.toLocalDateTime(): LocalDateTime {
    // Step 1: Get the time interval in seconds since the Unix epoch
    val timeInterval = (this.timeIntervalSince1970).toLong()

    // Step 2: Convert timeInterval to an Instant
    val instant = Instant.fromEpochSeconds(timeInterval.toLong())

    // Step 3: Convert Instant to LocalDateTime
    val zoneId = TimeZone.currentSystemDefault()
    return instant.toLocalDateTime(zoneId)
}

@OptIn(ExperimentalForeignApi::class)
fun requestCalendarAccess(completion: (granted: Boolean, error: NSError?) -> Unit) {
    val eventStore = EKEventStore()

    eventStore.requestAccessToEntityType(EKEntityType.EKEntityTypeEvent) { granted, error ->
        completion(granted, error)
    }
}

class iOSPlatform(private val vc: NavHostController) : Platform {
    override val navController: NavHostController
        @Composable get() = vc

    @OptIn(ExperimentalForeignApi::class)
    override fun addCalendarEvent(remindDto: RemindDto): String {

        val eventStore = EKEventStore()
        val event = EKEvent.eventWithEventStore(eventStore)

        // 配置事件属性
        event.title = remindDto.title
        event.notes = remindDto.details
        event.startDate = remindDto.startDate.toNSDate()
        event.endDate = remindDto.endDate.toNSDate()
        event.calendar = eventStore.defaultCalendarForNewEvents
        event.addAlarm(EKAlarm().apply {
            relativeOffset = -(remindDto.beforeMinutes.toDouble() * 60)
        })

        // 保存事件
        eventStore.saveEvent(event, EKSpan.EKSpanFutureEvents, null)
        return event.eventIdentifier!!

    }

    override fun queryCalendarEvents(): List<EventRemind> {
        val systemRemindArray = mutableListOf<EventRemind>()
        val eventStore = EKEventStore()

        // 获取所有日历
        val calendars: List<EKCalendar> =
            eventStore.calendarsForEntityType(EKEntityType.EKEntityTypeEvent) as List<EKCalendar>
        val userCalendars = calendars.filter { it.type == EKCalendarType.EKCalendarTypeCalDAV }

        userCalendars.map { calendar ->
            val predicate = eventStore.predicateForEventsWithStartDate(
                getTodayZero().toNSDate(),
                LocalDateTime(2999, 12, 31, 23, 59, 59).toNSDate(),
                listOf(calendar)
            )
            val event: List<EKEvent> =
                eventStore.eventsMatchingPredicate(predicate) as List<EKEvent>
            event.filter { (it.startDate!!.timeIntervalSince1970() * 1000).toLong() >= getTodayZero().toLong() }
                .forEach {
                    var isAlarm: Boolean = false
                    var alarmTime = 0
                    // 是否有提醒
                    if (it.alarms != null && it.alarms!!.isNotEmpty()) {
                        val alarms = it.alarms as List<EKAlarm>
                        alarmTime = -(alarms[0].relativeOffset / 60).toInt()
                        isAlarm = alarmTime > 0
                    }

                    systemRemindArray.add(
                        EventRemind(
                            it.eventIdentifier!!, RemindDto(
                                title = it.title ?: "",
                                details = it.notes ?: "",
                                startDate = it.startDate!!.toLocalDateTime(),
                                endDate = it.endDate!!.toLocalDateTime(),
                                alarm = isAlarm,
                                _beforeMinutes = alarmTime
                            )
                        )
                    )
                }
        }


        return systemRemindArray
    }

    override fun getCurrentLanguage(): String {
        UIImagePickerController()
        val locale = NSLocale.currentLocale
        return locale.languageCode
    }

    @OptIn(ExperimentalForeignApi::class)
    @Composable
    override fun SelectImageCompose(
        fileDataList: List<FileData?>,
        addFile: (ImageBitmap) -> Unit,
        delFile: (ImageBitmap) -> Unit,
        content: @Composable (
            imageBitmapArray: List<ImageBitmap>, addPhoto: () -> Unit, delImageChange: (ImageBitmap) -> Unit
        ) -> Unit
    ) {
        val imagePicker = UIImagePickerController()

        // 使用 snapshotFlow 来监听 fileDataList 的变化
        val imageBitmapListState by snapshotFlow { fileDataList.mapNotNull { it?.imageBitmap } }
            .collectAsState(initial = emptyList())

        // 图片选择窗口的委托
        val galleryDelegate = remember {
            object : NSObject(), UIImagePickerControllerDelegateProtocol,
                UINavigationControllerDelegateProtocol {
                override fun imagePickerController(
                    picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>
                ) {
                    // 确保只获取原始图片
                    val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage

                    // 将图片转换为字节数组
                    val byteArray: ByteArray? = image?.let {
                        val imageData = UIImageJPEGRepresentation(it, 0.99) // 高质量压缩
                        val bytes = imageData?.bytes?.reinterpret<ByteVar>()
                        val length = imageData?.length?.toInt() ?: 0
                        ByteArray(length) { index -> bytes!![index] }
                    }

                    // 将字节数组转换为 ImageBitmap
                    val img = byteArray?.let {
                        Image.makeFromEncoded(it).toComposeImageBitmap()
                    }

                    // 添加文件到列表
                    img?.let { addFile(it) }

                    // 关闭图片选择器
                    picker.dismissViewControllerAnimated(true, null)
                }
            }
        }

        // 配置 UIImagePickerController
        imagePicker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        imagePicker.allowsEditing = false // 禁用编辑以确保获取原图
        imagePicker.setDelegate(galleryDelegate)

        // 使用传入的 content Composable 来处理界面展示
        content(imageBitmapListState, {
            // 打开图片选择器
            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                imagePicker, true, null
            )
        }, { delFile(it) })
    }

    override fun saveFileImage(imageBitmap: ImageBitmap): String {
        val imageData = UIImageJPEGRepresentation(imageBitmap.toUIImage(), 0.99)
        val fileName = NSUUID().UUIDString + ".png"
        val fileManager = NSFileManager.defaultManager
        val documentsDirectory = fileManager.URLsForDirectory(
            platform.Foundation.NSDocumentDirectory, platform.Foundation.NSUserDomainMask
        ).firstOrNull() as? NSURL

        // 创建文件路径
        val fileURL = documentsDirectory?.URLByAppendingPathComponent(fileName)
        imageData!!.writeToURL(fileURL!!, true)
        return fileURL.toString()
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun downFileImage(filePath: String): ImageBitmap? {
        // ios模拟器中会刷新掉保存的图片
        return withContext(Dispatchers.IO) {
            // 使用 NSData 读取文件
            val nsData = NSData.dataWithContentsOfFile(filePath.removePrefix("file://"))
            // 如果读取成功，使用 UIImage 加载图片
            val uiImage = nsData?.let { UIImage(it) } ?: return@withContext null
            // 转换为 ImageBitmap
            uiImage.let {
                val imageData = UIImageJPEGRepresentation(it, 0.99)
                val bytes = imageData?.bytes?.reinterpret<ByteVar>()
                val length = imageData?.length?.toInt() ?: 0
                ByteArray(length) { index -> bytes!![index] }
            }.let {
                Image.makeFromEncoded(it).toComposeImageBitmap()
            }
        }

    }
}

actual fun isSameWeek(
    year1: Int, month1: Int, day1: Int, year2: Int, month2: Int, day2: Int
): Boolean {
    val calendar = NSCalendar.currentCalendar
    // 创建日期组件
    val dateComponents1 = NSDateComponents().apply {
        this.year = year1.toLong()
        this.month = month1.toLong()
        this.day = day1.toLong()
    }

    val dateComponents2 = NSDateComponents().apply {
        this.year = year2.toLong()
        this.month = month2.toLong()
        this.day = day2.toLong()
    }

    // 创建日期对象
    val date1 = calendar.dateFromComponents(dateComponents1)!!
    val date2 = calendar.dateFromComponents(dateComponents2)!!
    val currentDate = NSDate()

    // 判断日期1和当前日期是否在同一周
    val isDate1SameWeekAsCurrent = calendar.isDate(
        date1, equalToDate = currentDate, toUnitGranularity = NSCalendarUnitWeekOfYear
    )

    // 判断日期2和当前日期是否在同一周
    val isDate2SameWeekAsCurrent = calendar.isDate(
        date2, equalToDate = currentDate, toUnitGranularity = NSCalendarUnitWeekOfYear
    )

    return isDate1SameWeekAsCurrent && isDate2SameWeekAsCurrent
}
