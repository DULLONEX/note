package data.entiry

data class ChargeUpDto(

    // 内容
    val content:String = "",

    // 金额
    val amount:String = "0",

    // 金额类型
    val amountType: AmountTypeDto = AmountTypeDto(0, ""),

    // files
    val files:MutableList<FileData> = mutableListOf()
)