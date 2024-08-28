package data.service

import data.Database
import data.entiry.AmountTypeDto
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface AmountTypeService{

  suspend fun saveAmountType(amountTypeDto: AmountTypeDto)

  suspend fun updateAmountType(amountTypeDto: AmountTypeDto)

  suspend fun findAmountTypeAll():List<AmountTypeDto>
}

class AmountTypeServiceImpl : AmountTypeService,KoinComponent{
    private val database: Database by inject()


    override suspend fun saveAmountType(amountTypeDto: AmountTypeDto) {
        TODO("Not yet implemented")
    }

    override suspend fun updateAmountType(amountTypeDto: AmountTypeDto) {
        TODO("Not yet implemented")
    }

    override suspend fun findAmountTypeAll(): List<AmountTypeDto> {
        TODO("Not yet implemented")
    }

}