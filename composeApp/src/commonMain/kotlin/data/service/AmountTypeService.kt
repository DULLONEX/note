package data.service

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import data.Database
import data.entiry.AmountTypeDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface AmountTypeService {

    suspend fun saveAmountType(message:String)

    suspend fun updateAmountType(id:Long,message:String)

    suspend fun findAmountTypeAll(): Flow<List<AmountTypeDto>>
}

class AmountTypeServiceImpl : AmountTypeService, KoinComponent {
    private val database: Database by inject()


    override suspend fun saveAmountType(message:String) {
        database.amountTypeQueries.insertAmountType(message)
    }

    override suspend fun updateAmountType(id:Long,message:String) {
        database.amountTypeQueries.updateAmountType(message, id)
    }

    override suspend fun findAmountTypeAll(): Flow<List<AmountTypeDto>> {
        return database.amountTypeQueries.findAmountTypeAll().asFlow().mapToList(Dispatchers.IO)
            .map { array ->
                array.map {
                    AmountTypeDto(it.id, it.message, whetherSystem = it.whetherSystem)
                }
            }
    }

}