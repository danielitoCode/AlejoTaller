package com.elitec.alejotallerscan.feature.product.domain.caseuse

import com.elitec.alejotallerscan.feature.finance.domain.entity.SaleFinanceLineWrite
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

    private fun costTriple(existence: Int, reserved: Int, cost: Double?): Triple<Int, Int, Double?> =
        Triple(existence, reserved, cost)

    @Test
    fun verified_writes_salida_venta_and_finance_with_lines() = runBlocking {
        val stock = FakeStockRepo(
            mutableMapOf(
                "p1" to costTriple(10, 2, 5.0),
                "p2" to costTriple(20, 1, 3.0)
            )
        )
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
        assertEquals(2, fin.lines.size)
        val lineP1 = fin.lines.first { it.productId == "p1" }
        assertEquals(2, lineP1.quantity)
        assertEquals(40.0, lineP1.unitPrice, 0.001)
        assertEquals(5.0, lineP1.unitCostSnapshot, 0.001)
        assertEquals(80.0, lineP1.lineRevenue, 0.001)
        assertEquals(10.0, lineP1.lineCogs, 0.001)
        assertEquals(70.0, lineP1.lineMargin, 0.001)
        val lineP2 = fin.lines.first { it.productId == "p2" }
        assertEquals(3.0, lineP2.unitCostSnapshot, 0.001)
        assertEquals(3.0, lineP2.lineCogs, 0.001)
    }

    @Test
    fun deleted_only_releases_reserved_no_movement_no_finance() = runBlocking {
        val stock = FakeStockRepo(mutableMapOf("p1" to costTriple(10, 2, 5.0)))
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
        val stock = FakeStockRepo(mutableMapOf("p1" to costTriple(10, 1, 4.0)))
        val movements = FakeMovementRepo(
            existing = listOf(
                StockMovementRecord("m1", "p1", "salida_venta", 1, 9, "sale-1")
            )
        )
        val finance = FakeFinanceRepo(
            seed = SaleFinanceRecord(
                id = "f1",
                saleId = "sale-1",
                revenue = 50.0,
                cogs = 4.0,
                margin = 46.0,
                lines = listOf(
                    SaleFinanceLineWrite(
                        productId = "p1",
                        quantity = 1,
                        unitPrice = 50.0,
                        unitCostSnapshot = 4.0,
                        lineRevenue = 50.0,
                        lineCogs = 4.0,
                        lineMargin = 46.0
                    )
                )
            )
        )
        val useCase = ApplyOperatorStockDecisionCaseUse(
            stock, movements, finance, FakeAccountRepo("op-1")
        )

        val result = useCase(sale(listOf(SaleItem("p1", 1)), amount = 50.0), confirmed = true)

        assertTrue(result.isSuccess)
        assertTrue(movements.created.isEmpty())
        assertEquals(1, finance.createCalls)
        assertEquals(0, finance.created.size)
        val stored = finance.getBySaleId("sale-1")!!
        assertEquals(4.0, stored.cogs, 0.001)
        assertEquals(4.0, stored.lines.single().unitCostSnapshot, 0.001)
    }

    @Test
    fun verified_missing_cost_uses_zero_snapshot() = runBlocking {
        val stock = FakeStockRepo(mutableMapOf("p1" to costTriple(5, 1, null)))
        val movements = FakeMovementRepo()
        val finance = FakeFinanceRepo()
        val useCase = ApplyOperatorStockDecisionCaseUse(
            stock, movements, finance, FakeAccountRepo("op-1")
        )

        val result = useCase(
            sale(listOf(SaleItem("p1", 1, unitPrice = 30.0)), amount = 30.0),
            confirmed = true
        )

        assertTrue(result.isSuccess)
        val fin = finance.created.single()
        assertEquals(0.0, fin.cogs, 0.001)
        assertEquals(0.0, fin.lines.single().unitCostSnapshot, 0.001)
        assertEquals(30.0, fin.margin, 0.001)
    }

    /**
     * Core 4 B4 — tras el primer finance, un last_unit_cost vivo distinto
     * no muta cogs ni unitCostSnapshot del event almacenado.
     */
    @Test
    fun b4_second_confirm_does_not_rewrite_finance_when_cost_changes() = runBlocking {
        val stock = FakeStockRepo(
            mutableMapOf("p1" to costTriple(10, 2, 5.0))
        )
        val movements = FakeMovementRepo()
        val finance = FakeFinanceRepo()
        val useCase = ApplyOperatorStockDecisionCaseUse(
            stock, movements, finance, FakeAccountRepo("op-1")
        )
        val s = sale(listOf(SaleItem("p1", 2, unitPrice = 40.0)), amount = 80.0)

        val first = useCase(s, confirmed = true)
        assertTrue(first.isSuccess)
        assertEquals(1, finance.created.size)
        assertEquals(10.0, finance.created.single().cogs, 0.001)
        assertEquals(5.0, finance.created.single().lines.single().unitCostSnapshot, 0.001)

        // Costo vivo cambia (p.ej. nueva compra); reintento de confirm
        stock.state["p1"] = costTriple(8, 0, 99.0)
        val second = useCase(s, confirmed = true)
        assertTrue(second.isSuccess)

        assertEquals(2, finance.createCalls)
        assertEquals(1, finance.created.size) // no segundo documento
        val stored = finance.getBySaleId("sale-1")!!
        assertEquals(10.0, stored.cogs, 0.001)
        assertEquals(5.0, stored.lines.single().unitCostSnapshot, 0.001)
        assertTrue(stored.lines.none { it.unitCostSnapshot == 99.0 })
    }

    /** Core 4 B4 — createIdempotent a nivel repo: payload nuevo no sobrescribe. */
    @Test
    fun b4_createIdempotent_returns_frozen_event_ignoring_new_payload() = runBlocking {
        val finance = FakeFinanceRepo()
        val firstWrite = SaleFinanceWrite(
            saleId = "sale-x",
            revenue = 100.0,
            cogs = 20.0,
            margin = 80.0,
            userId = "op-1",
            atIso = "2026-09-02T00:00:00Z",
            currency = "USD",
            lines = listOf(
                SaleFinanceLineWrite(
                    productId = "p1",
                    quantity = 2,
                    unitPrice = 50.0,
                    unitCostSnapshot = 10.0,
                    lineRevenue = 100.0,
                    lineCogs = 20.0,
                    lineMargin = 80.0
                )
            )
        )
        val first = finance.createIdempotent(firstWrite)
        assertEquals(10.0, first.lines.single().unitCostSnapshot, 0.001)

        val rewritten = finance.createIdempotent(
            firstWrite.copy(
                cogs = 999.0,
                margin = -899.0,
                lines = listOf(
                    SaleFinanceLineWrite(
                        productId = "p1",
                        quantity = 2,
                        unitPrice = 50.0,
                        unitCostSnapshot = 999.0,
                        lineRevenue = 100.0,
                        lineCogs = 1998.0,
                        lineMargin = -1898.0
                    )
                )
            )
        )

        assertEquals(2, finance.createCalls)
        assertEquals(1, finance.created.size)
        assertEquals(first.id, rewritten.id)
        assertEquals(20.0, rewritten.cogs, 0.001)
        assertEquals(10.0, rewritten.lines.single().unitCostSnapshot, 0.001)
    }

    /** Un solo constructor primario: evita platform declaration clash Map/MutableMap en JVM. */
    private class FakeStockRepo(
        val state: MutableMap<String, Triple<Int, Int, Double?>>
    ) : OperatorStockRepository {
        val lastExistence = mutableMapOf<String, Int>()
        val lastReserved = mutableMapOf<String, Int>()

        override suspend fun applyDeltas(
            productId: String,
            existenceDelta: Int,
            reservedDelta: Int
        ): OperatorStockSnapshot {
            val (ex, res, cost) = state[productId] ?: Triple(0, 0, null as Double?)
            val nextEx = (ex + existenceDelta).coerceAtLeast(0)
            val nextRes = (res + reservedDelta).coerceAtLeast(0)
            state[productId] = Triple(nextEx, nextRes, cost)
            lastExistence[productId] = nextEx
            lastReserved[productId] = nextRes
            return OperatorStockSnapshot(nextEx, nextRes, cost)
        }
    }

    private class FakeMovementRepo(
        existing: List<StockMovementRecord> = emptyList()
    ) : OperatorStockMovementRepository {
        private val bySale = existing
            .groupBy { it.saleId.orEmpty() }
            .mapValues { it.value.toMutableList() }
            .toMutableMap()
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

    /**
     * Paridad con AppwriteOperatorSaleFinanceRepository.createIdempotent:
     * guarda el primer event por sale_id y lo devuelve en reintentos.
     */
    private class FakeFinanceRepo(
        seed: SaleFinanceRecord? = null
    ) : OperatorSaleFinanceRepository {
        private val bySaleId = mutableMapOf<String, SaleFinanceRecord>()
        val created = mutableListOf<SaleFinanceWrite>()
        var createCalls = 0

        init {
            if (seed != null) bySaleId[seed.saleId] = seed
        }

        override suspend fun getBySaleId(saleId: String): SaleFinanceRecord? =
            bySaleId[saleId]

        override suspend fun createIdempotent(event: SaleFinanceWrite): SaleFinanceRecord {
            createCalls++
            val found = bySaleId[event.saleId]
            if (found != null) return found
            created += event
            val rec = SaleFinanceRecord(
                id = "new-${created.size}",
                saleId = event.saleId,
                revenue = event.revenue,
                cogs = event.cogs,
                margin = event.margin,
                lines = event.lines
            )
            bySaleId[event.saleId] = rec
            return rec
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
