package data.entiry

import androidx.compose.ui.graphics.ImageBitmap
import com.onex.note.ChargeUp
import com.onex.note.SelectAllChargeUp
import com.onex.note.SelectChargeUpByid
import config.convertLocalDateTime
import config.formatDateString
import config.getCurrentDateTime
import config.getCurrentDateTimeLong
import kotlinx.datetime.LocalDateTime

data class ChargeUpDto(
    // id
    val id:Long?,
    // 内容
    val content: String = "",

    // 金额
    val amount: String = "",

    // 金额类型
    val amountType: AmountTypeDto = AmountTypeDto(0, ""),

    // files
    val files: MutableList<FileData> = mutableListOf(),

    // 填写时间
    val fillTime: LocalDateTime = getCurrentDateTime(),

    // 创建时间
    val createTime: String = "",
)

data class MonthSumCharge(
    var sumAmount: String = "0",
    var currentDate: String = "",

)

fun  MutableList<FileData>.asBitmapList(): List<ImageBitmap> {
   return this.filter { it.imageBitmap != null }.map { it.imageBitmap!! }.toList()
}


fun SelectAllChargeUp.toChargeUpDto(): ChargeUpDto {

    return ChargeUpDto(
        this.id,
        this.content,
        this.amount,
        AmountTypeDto(this.amountTypeId, this.message,this.whetherSystem),
        mutableListOf(),
        convertLocalDateTime(this.fillTime),
        formatDateString(this.createTime)
    )
}
