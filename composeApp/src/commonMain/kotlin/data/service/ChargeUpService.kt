package data.service

import Platform
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import config.formatDateString
import config.getCurrentDateTimeLong
import data.Database
import data.entiry.AmountTypeDto
import data.entiry.ChargeUpDto
import data.entiry.FileData
import data.entiry.MonthSumCharge
import data.entiry.toChargeUpDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface ChargeUpService {
    // 实现保存逻辑
    fun saveChargeUp(chargeUpDto: ChargeUpDto)

    fun updateChargeUp(chargeUpDto: ChargeUpDto)

    fun findAllChargeUp(): Flow<Map<MonthSumCharge, List<ChargeUpDto>>>

    fun deleteChargeUp(id: Long)

    suspend fun findChargeUpById(id: Long): ChargeUpDto
}

class ChargeUpServiceImpl : ChargeUpService, KoinComponent {
    private val platform: Platform by inject()

    private val database: Database by inject()

    override fun saveChargeUp(chargeUpDto: ChargeUpDto) {
        // 保存图片补上路径
        chargeUpDto.files.filter { it.path == null }.forEach {
            it.path = platform.saveFileImage(it.imageBitmap!!)
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

    override fun updateChargeUp(chargeUpDto: ChargeUpDto) {
        // 保存图片补上路径
        chargeUpDto.files.filter { it.path == null }.forEach {
            it.path = platform.saveFileImage(it.imageBitmap!!)
        }
        database.chargeUpQueries.updateChargeUp(
            chargeUpDto.content,
            chargeUpDto.amount,
            chargeUpDto.files.joinToString(separator = ",") { it.path!! },
            chargeUpDto.amountType.id,
            chargeUpDto.id!!
        )
    }

    override fun findAllChargeUp(): Flow<Map<MonthSumCharge, List<ChargeUpDto>>> {
        return database.chargeUpQueries.selectAllChargeUp().asFlow().mapToList(Dispatchers.IO)
            .map { array ->
                array.map { chargeUp ->
                    chargeUp.toChargeUpDto()
                }.groupBy(keySelector = {
                    val split = it.createTime.split("/")
                    "${split[0]}/${split[1]}"
                }).mapKeys { map ->
                    MonthSumCharge(map.value.sumOf { it.amount.toDouble() }.toString(), map.key)
                }
            }
    }

    override fun deleteChargeUp(id: Long) {
        database.chargeUpQueries.delChargeUp(id)
    }

    override suspend fun findChargeUpById(id: Long): ChargeUpDto {
        database.chargeUpQueries.selectChargeUpByid(id).executeAsOne().let {
            // 处理files
            val files = it.files?.split(",")?.map fileMap@{ path ->
                return@fileMap withContext(Dispatchers.IO) {
                    FileData(path, platform.downFileImage(path))
                }
            }?.toMutableList()

            val dto = ChargeUpDto(
                it.id,
                it.content,
                it.amount,
                AmountTypeDto(it.amountTypeId, it.message),
                files = files ?: mutableListOf(),
                formatDateString(it.createTime)
            )
            return dto
        }
    }
}