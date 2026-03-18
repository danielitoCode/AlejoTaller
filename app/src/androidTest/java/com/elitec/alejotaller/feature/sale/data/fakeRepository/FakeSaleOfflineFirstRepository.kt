package com.elitec.alejotaller.feature.sale.data.fakeRepository

import com.elitec.alejotaller.feature.sale.domain.entity.BuyState
import com.elitec.alejotaller.feature.sale.domain.entity.DeliveryType
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.entity.SaleItem
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class FakeSaleOfflineFirstRepository: SaleRepository {
    private val fakeSaleListFlow = MutableStateFlow(listOf<Sale>())

    private val fakeSale = listOf(
        Sale(
            id = "sale id 1",
            date = LocalDate(2026,3,24),
            amount = 45.8,
            verified = BuyState.VERIFIED,
            products = listOf(
                SaleItem("1",3),
                SaleItem("3",1)
            ),
            userId = "userId test",
            deliveryType = DeliveryType.DELIVERY
        ),
        Sale(
            id = "sale id 2",
            date = LocalDate(2026,3,25),
            amount = 45.8,
            verified = BuyState.VERIFIED,
            products = listOf(
                SaleItem("1",3),
                SaleItem("2",1)
            ),
            userId = "userId test",
            deliveryType = DeliveryType.DELIVERY
        ),
        Sale(
            id = "sale id 3",
            date = LocalDate(2026,3,26),
            amount = 42.8,
            verified = BuyState.VERIFIED,
            products = listOf(
                SaleItem("2",3),
                SaleItem("3",1)
            ),
            userId = "userId test",
            deliveryType = DeliveryType.DELIVERY
        )
    )

    override fun observeAll(): Flow<List<Sale>> = fakeSaleListFlow.asStateFlow()

    override suspend fun getById(itemId: String): Sale {
        val fakeSaleListen = fakeSaleListFlow.last()
        return fakeSaleListen.first { category -> category.id == itemId }
    }

    override suspend fun save(item: Sale) {}

    override suspend fun sync(userId: String): Result<Unit> = runCatching {
        fakeSale.forEachIndexed { index, sale ->
            fakeSaleListFlow.update { list ->
                val tempSaleList = list.toMutableList()
                tempSaleList.add(sale)
                tempSaleList
            }
            delay(400L * index)
        }
    }
}