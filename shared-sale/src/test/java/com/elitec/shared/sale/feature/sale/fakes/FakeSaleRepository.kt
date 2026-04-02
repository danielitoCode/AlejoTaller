package com.elitec.shared.sale.feature.sale.fakes

import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.entity.SaleItem
import com.elitec.shared.sale.feature.sale.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

class FakeSaleRepository(
    initialSale: Sale = sampleSale()
) : SaleRepository {
    var currentSale: Sale = initialSale
    var savedSales: MutableList<Sale> = mutableListOf()
    var syncUserId: String? = null
    var syncResult: Result<Unit> = Result.success(Unit)

    override fun observeAll(): Flow<List<Sale>> = flowOf(listOf(currentSale))

    override suspend fun getById(itemId: String): Sale = currentSale

    override suspend fun save(item: Sale) {
        currentSale = item
        savedSales += item
    }

    override suspend fun sync(userId: String): Result<Unit> {
        syncUserId = userId
        return syncResult
    }
}

fun sampleSale(
    id: String = "sale-1",
    verified: BuyState = BuyState.UNVERIFIED
): Sale = Sale(
    id = id,
    date = LocalDate(2026, 4, 2),
    amount = 12.5,
    verified = verified,
    products = listOf(
        SaleItem(
            productId = "product-1",
            quantity = 1,
            productName = "Battery"
        )
    ),
    userId = "user-1"
)
