package com.elitec.alejotallerscan.feature.product.domain.caseuse

import com.elitec.alejotallerscan.feature.finance.domain.entity.SaleFinanceRecord
import com.elitec.alejotallerscan.feature.finance.domain.entity.SaleFinanceWrite
import com.elitec.alejotallerscan.feature.finance.domain.repository.OperatorSaleFinanceRepository
import com.elitec.alejotallerscan.feature.inventory.domain.entity.StockMovementRecord
import com.elitec.alejotallerscan.feature.inventory.domain.entity.StockMovementWrite
import com.elitec.alejotallerscan.feature.inventory.domain.repository.OperatorStockMovementRepository
import com.elitec.alejotallerscan.feature.product.domain.repository.OperatorStockRepository
import com.elitec.alejotallerscan.feature.product.domain.repository.OperatorStockSnapshot
import com.elitec.shared.auth.feature.auth.domain.entity.User
import com.elitec.shared.auth.feature.auth.domain.entity.UserProfile
import com.elitec.shared.auth.feature.auth.domain.repositories.AccountRepository
import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
import com.elitec.shared.sale.feature.sale.domain.entity.Currency
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.entity.SaleItem
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplyOperatorStockDecisionCaseUseTest {

    private fun sale(products: List<SaleItem>, amount: Double = 100.0) = Sale(
        id = "sale-1",
        date = LocalDate(2026, 8, 19),
        amount = amount,
        currency = Currency.CUP,
        verified = BuyState.VERIFIED,
        products = products,
        userId = "client-1"
    )

    @Test
    fun verified_writes_salida_venta_and_finance() = runBlocking {
        val stock = FakeStockRepo(mapOf("p1" to Triple(10, 2, 5.0), "p2" to Triple(20, 1, 3.0)))
        val movements = FakeMovementRepo()
        val finance = FakeFinanceRepo()
        val useCase = ApplyOperatorStockDecisionCaseUse(
            stock, movements, finance, FakeAccountRepo("op-1")
        )

        val result = useCase(
            sale(
                listOf(
                    SaleItem("p1", 2, unitPrice = 40.0),
                    SaleItem("p2", 1, unitPrice = 20.0)
                ),
                amount = 100.0
            ),
            confirmed = true
        )

        assertTrue(result.isSuccess)
        assertEquals(2, movements.created.size)
        assertTrue(movements.created.all { it.type == "salida_venta" })
        assertEquals(8, movements.created.first { it.productId == "p1" }.balanceAfter)
        assertEquals(19, movements.created.first { it.productId == "p2" }.balanceAfter)
        val fin = finance.created.single()
        assertEquals(100.0, fin.revenue, 0.001)
        assertEquals(13.0, fin.cogs, 0.001)
        assertEquals(87.0, fin.margin, 0.001)
        assertEquals("sale-1", fin.saleId)
    }

    @Test
    fun deleted_only_releases_reserved_no_movement_no_finance() = runBlocking {
        val stock = FakeStockRepo(mapOf("p1" to Triple(10, 2, 5.0)))
        val movements = FakeMovementRepo()
        val finance = FakeFinanceRepo()
        val useCase = ApplyOperatorStockDecisionCaseUse(
            stock, movements, finance, FakeAccountRepo("op-1")
        )

        val result = useCase(
            sale(listOf(SaleItem("p1", 2))),
            confirmed = false
        )

        assertTrue(result.isSuccess)
        assertTrue(movements.created.isEmpty())
        assertTrue(finance.created.isEmpty())
        assertEquals(10, stock.lastExistence["p1"])
        assertEquals(0, stock.lastReserved["p1"])
    }

    @Test
    fun verified_idempotent_skips_existing_movement_and_finance() = runBlocking {
        val stock = FakeStockRepo(mapOf("p1" to Triple(10, 1, 4.0)))
        val movements = FakeMovementRepo(
            existing = listOf(
                StockMovementRecord("m1", "p1", "salida_venta", 1, 9, "sale-1")
            )
        )
        val finance = FakeFinanceRepo(
            existing = SaleFinanceRecord("f1", "sale-1", 50.0, 4.0, 46.0)
        )
        val useCase = ApplyOperatorStockDecisionCaseUse(
            stock, movements, finance, FakeAccountRepo("op-1")
        )

        val result = useCase(sale(listOf(SaleItem("p1", 1)), amount = 50.0), confirmed = true)

        assertTrue(result.isSuccess)
        assertTrue(movements.created.isEmpty())
        assertEquals(1, finance.createCalls)
        assertEquals(0, finance.created.size)
    }

    private class FakeStockRepo(
        // Triple es invariante: mapOf(... to Triple(..., 5.0)) infiere Triple<Int,Int,Double>
        // y no es asignable a Triple<Int,Int,Double?> → CompilationErrorException en CI
        private val state: Map<String, Triple<Int, Int, Double>>
    ) : OperatorStockRepository {
        val lastExistence = mutableMapOf<String, Int>()
        val lastReserved = mutableMapOf<String, Int>()

        override suspend fun applyDeltas(
            productId: String,
            existenceDelta: Int,
            reservedDelta: Int
        ): OperatorStockSnapshot {
            val (ex, res, cost) = state[productId] ?: Triple(0, 0, 0.0)
            val nextEx = (ex + existenceDelta).coerceAtLeast(0)
            val nextRes = (res + reservedDelta).coerceAtLeast(0)
            lastExistence[productId] = nextEx
            lastReserved[productId] = nextRes
            return OperatorStockSnapshot(nextEx, nextRes, cost)
        }
    }

    private class FakeMovementRepo(
        existing: List<StockMovementRecord> = emptyList()
    ) : OperatorStockMovementRepository {
        private val bySale = existing.groupBy { it.saleId.orEmpty() }.mapValues { it.value.toMutableList() }
        val created = mutableListOf<StockMovementWrite>()

        override suspend fun listBySaleId(saleId: String): List<StockMovementRecord> =
            bySale[saleId].orEmpty()

        override suspend fun create(movement: StockMovementWrite): StockMovementRecord {
            created += movement
            val rec = StockMovementRecord(
                id = "new-${created.size}",
                productId = movement.productId,
                type = movement.type,
                quantity = movement.quantity,
                balanceAfter = movement.balanceAfter,
                saleId = movement.saleId
            )
            bySale.getOrPut(movement.saleId.orEmpty()) { mutableListOf() }.add(rec)
            return rec
        }
    }

    private class FakeFinanceRepo(
        private val existing: SaleFinanceRecord? = null
    ) : OperatorSaleFinanceRepository {
        val created = mutableListOf<SaleFinanceWrite>()
        var createCalls = 0

        override suspend fun getBySaleId(saleId: String): SaleFinanceRecord? =
            existing?.takeIf { it.saleId == saleId }

        override suspend fun createIdempotent(event: SaleFinanceWrite): SaleFinanceRecord {
            createCalls++
            val found = getBySaleId(event.saleId)
            if (found != null) return found
            created += event
            return SaleFinanceRecord("new", event.saleId, event.revenue, event.cogs, event.margin)
        }
    }

    private class FakeAccountRepo(private val id: String) : AccountRepository {
        override suspend fun getCurrentUserInfo(): User = User(
            id = id,
            name = "Op",
            email = "op@test.com",
            pass = "",
            userProfile = UserProfile(sub = id, role = "operator")
        )
    }
}
