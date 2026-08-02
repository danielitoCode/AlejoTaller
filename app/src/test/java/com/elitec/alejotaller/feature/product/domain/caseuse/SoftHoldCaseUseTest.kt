package com.elitec.alejotaller.feature.product.domain.caseuse

import com.elitec.alejotaller.data.fakesRepositories.product.FakeProductRepository
import com.elitec.alejotaller.feature.product.domain.caseUse.ApplySoftHoldCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.CheckAProductExistenceCaseUse
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
import com.elitec.shared.sale.feature.sale.domain.entity.Currency
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.entity.SaleItem
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests parciales Core 1 — SALE_POLICY + WAREHOUSE_POLICY (cliente Android).
 * Cubre: available = existence - reserved, soft-hold y idempotencia stockHoldApplied.
 */
class SoftHoldCaseUseTest {

    @Test
    fun `availableStock is existence minus reserved`() {
        val product = fakeProduct(existence = 10, reserved = 3)
        assertEquals(7, product.availableStock())
    }

    @Test
    fun `availableStock never goes negative`() {
        val product = fakeProduct(existence = 2, reserved = 5)
        assertEquals(0, product.availableStock())
    }

    @Test
    fun `check passes when available covers quantity`() = runTest {
        val repo = FakeProductRepository(
            products = listOf(fakeProduct(id = "p1", existence = 10, reserved = 2))
        )
        val check = CheckAProductExistenceCaseUse(repo)

        val result = check(sampleSale(quantity = 8))

        assertTrue(result.isSuccess)
    }

    @Test
    fun `check fails when reserved reduces available below quantity`() = runTest {
        val repo = FakeProductRepository(
            products = listOf(fakeProduct(id = "p1", existence = 10, reserved = 6))
        )
        val check = CheckAProductExistenceCaseUse(repo)

        val result = check(sampleSale(quantity = 5))

        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull()?.message?.contains("No hay disponibilidad") == true
        )
    }

    @Test
    fun `soft-hold increments reserved when stock is available`() = runTest {
        val product = fakeProduct(id = "p1", existence = 10, reserved = 1)
        val repo = FakeProductRepository(products = listOf(product))
        val hold = ApplySoftHoldCaseUse(repo)

        val result = hold(sampleSale(quantity = 3, stockHoldApplied = false))

        assertTrue(result.isSuccess)
        assertEquals(listOf("p1" to 3), repo.incrementReservedCalls)
        assertEquals(4, repo.getById("p1")?.reserved)
        assertEquals(6, repo.getById("p1")?.availableStock())
    }

    @Test
    fun `soft-hold is skipped when stockHoldApplied is true`() = runTest {
        val repo = FakeProductRepository(
            products = listOf(fakeProduct(id = "p1", existence = 10, reserved = 0))
        )
        val hold = ApplySoftHoldCaseUse(repo)

        val result = hold(sampleSale(quantity = 2, stockHoldApplied = true))

        assertTrue(result.isSuccess)
        assertTrue(repo.incrementReservedCalls.isEmpty())
        assertEquals(0, repo.getById("p1")?.reserved)
    }

    @Test
    fun `soft-hold fails when available is insufficient`() = runTest {
        val repo = FakeProductRepository(
            products = listOf(fakeProduct(id = "p1", existence = 5, reserved = 3))
        )
        val hold = ApplySoftHoldCaseUse(repo)

        val result = hold(sampleSale(quantity = 3, stockHoldApplied = false))

        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull()?.message?.contains("Stock insuficiente") == true
        )
        assertTrue(repo.incrementReservedCalls.isEmpty())
    }

    private fun fakeProduct(
        id: String = "p1",
        existence: Int = 10,
        reserved: Int = 0
    ) = Product(
        id = id,
        name = "Product $id",
        description = "Description",
        price = 1.0,
        photoUrl = "https://test.local/$id.png",
        categoryId = "cat",
        existence = existence,
        reserved = reserved
    )

    private fun sampleSale(
        quantity: Int = 1,
        stockHoldApplied: Boolean = false
    ) = Sale(
        id = "sale-1",
        date = LocalDate(2026, 8, 2),
        amount = 10.0,
        currency = Currency.USD,
        verified = BuyState.UNVERIFIED,
        products = listOf(
            SaleItem(
                productId = "p1",
                productName = "Product p1",
                quantity = quantity
            )
        ),
        userId = "user-1",
        stockHoldApplied = stockHoldApplied
    )
}
