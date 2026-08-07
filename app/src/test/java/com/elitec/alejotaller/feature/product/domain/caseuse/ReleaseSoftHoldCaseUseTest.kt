package com.elitec.alejotaller.feature.product.domain.caseuse

import com.elitec.alejotaller.data.fakesRepositories.product.FakeProductRepository
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
 * Core 1: valida que el rollback delega la mutación a decrementReserved,
 * que en producción es una operación atómica de Appwrite.
 */
class ReleaseSoftHoldCaseUseTest {

    @Test
    fun `release soft-hold delegates to decrementReserved`() = runTest {
        val repo = FakeProductRepository(
            products = listOf(fakeProduct(id = "p1", existence = 10, reserved = 4))
        )
        val release = ReleaseSoftHoldCaseUse(repo)

        val result = release(sampleSale(quantity = 3))

        assertTrue(result.isSuccess)
        assertEquals(listOf("p1" to 3), repo.decrementReservedCalls)
        assertEquals(1, repo.getById("p1")?.reserved)
    }

    @Test
    fun `release clamps reserved at zero`() = runTest {
        val repo = FakeProductRepository(
            products = listOf(fakeProduct(id = "p1", existence = 10, reserved = 2))
        )
        val release = ReleaseSoftHoldCaseUse(repo)

        val result = release(sampleSale(quantity = 5))

        assertTrue(result.isSuccess)
        assertEquals(0, repo.getById("p1")?.reserved)
    }

    private fun fakeProduct(
        id: String,
        existence: Int,
        reserved: Int
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

    private fun sampleSale(quantity: Int) = Sale(
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
        stockHoldApplied = true
    )
}
