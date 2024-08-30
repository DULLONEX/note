package data.service

import Platform
import config.getCurrentDateTimeLong
import data.Database
import data.entiry.ChargeUpDto
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ChargeUpService {
    // 实现保存逻辑
    fun saveChargeUp(chargeUpDto: ChargeUpDto)
}

class ChargeUpServiceImpl : ChargeUpService, KoinComponent {
    private val platform: Platform by inject()

    private val database: Database by inject()

    override fun saveChargeUp(chargeUpDto: ChargeUpDto) {
        // 保存图片补上路径
        chargeUpDto.files.filter { it.path == null }.forEach {
            it.path = platform.saveFileImage(it.imageBitmap)
        }
        val filePaths =
            chargeUpDto.files.filter { it.path != null }.joinToString(separator = ",") { it.path!! }
        database.chargeUpQueries.insertChargeUp(
            chargeUpDto.content,
            chargeUpDto.amount,
            filePaths,
            amountTypeId = chargeUpDto.amountType.id,
            getCurrentDateTimeLong()
        )
    }
}