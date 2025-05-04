package data.service

import Platform
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import config.convertLocalDateTime
import config.formatDateString
import config.formatToString
import config.getCurrentDateTimeLong
import config.toLong
import data.Database
import data.entiry.AmountTypeDto
import data.entiry.ChargeUpDto
import data.entiry.FileData
import data.entiry.MonthSumCharge
import data.entiry.SimpleChargeUpSheet
import data.entiry.toChargeUpDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import sumAmount

interface ChargeUpService {
    // 实现保存逻辑
    fun saveChargeUp(chargeUpDto: ChargeUpDto)

    fun updateChargeUp(chargeUpDto: ChargeUpDto)

    fun findAllChargeUp(): Flow<Map<MonthSumCharge, List<ChargeUpDto>>>

    fun deleteChargeUp(id: Long)

    fun findSimpleCharUpSheet():Flow<SimpleChargeUpSheet>

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
            fillTime = chargeUpDto.fillTime.toLong(),
            getCurrentDateTimeLong(),
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
            fillTime = chargeUpDto.fillTime.toLong(),
            chargeUpDto.id!!
        )
    }

    override fun findAllChargeUp(): Flow<Map<MonthSumCharge, List<ChargeUpDto>>> {
        return database.chargeUpQueries.selectAllChargeUp().asFlow().mapToList(Dispatchers.IO)
            .map { array ->
                array.map { chargeUp ->
                    chargeUp.toChargeUpDto()
                }.groupBy(keySelector = {
                    val split = it.fillTime.formatToString().split("/")
                    "${split[0]}/${split[1]}"
                }).mapKeys { map ->
                    val amountList = map.value.map { it.amount }.toList()
                    MonthSumCharge(sumAmount(amountList), map.key)
                }
            }
    }

    override fun deleteChargeUp(id: Long) {
        database.chargeUpQueries.delChargeUp(id)
    }

    override fun findSimpleCharUpSheet(): Flow<SimpleChargeUpSheet> {
        return combine(
            sumAmount(),
            monthAverageAmount()
        ) { total, average ->
            SimpleChargeUpSheet(
                totalAmount = total,
                averageAmount = average
            )
        }
    }

    fun sumAmount(): Flow<String> {
        return database.chargeUpQueries.sumAmount().asFlow().mapToOne(Dispatchers.IO).map {
            (it.totalAmount ?: 0).toString()
        }
    }

    fun monthAverageAmount(): Flow<String> {
        return database.chargeUpQueries.monthAverageAmount().asFlow().mapToOne(Dispatchers.IO).map {
            (it.average_amount ?: 0).toString()
        }
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
                convertLocalDateTime(it.fillTime),
                formatDateString(it.createTime)
            )
            return dto
        }
    }
}