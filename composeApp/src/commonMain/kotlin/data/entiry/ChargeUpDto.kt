package data.entiry

import com.onex.note.SelectAllChargeUp
import config.formatDateString

data class ChargeUpDto(
    // id
    val id:Long = 0L,
    // 内容
    val content: String = "",

    // 金额
    val amount: String = "",

    // 金额类型
    val amountType: AmountTypeDto = AmountTypeDto(0, ""),

    // files
    val files: MutableList<FileData> = mutableListOf(),

    // 创建时间
    val createTime: String = "",
)

data class MonthSumCharge(
    var sumAmount: String = "0",
    var currentDate: String = "",

)

fun SelectAllChargeUp.toChargeUpDto(): ChargeUpDto {
    return ChargeUpDto(
        this.id,
        this.content,
        this.amount,
        AmountTypeDto(this.amountTypeId, this.message),
        mutableListOf(),
        formatDateString(this.createTime)
    )
}

