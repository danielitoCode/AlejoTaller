package com.elitec.alejotaller.feature.product.domain.caseuse

import android.nfc.FormatException
import com.elitec.alejotaller.data.fakesRepositories.FakeProductRepository
import com.elitec.alejotaller.feature.product.domain.caseUse.GetProductByIdCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.ObserveProductsCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.SyncProductCaseUse
import com.elitec.alejotaller.feature.product.domain.entity.Product
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProductsCaseUseTest {
    @Test
    fun `observe products returns repository flow`() = runTest {
        val expected = listOf(fakeProduct(id = "p-1"), fakeProduct(id = "p-2"))
        val repository = FakeProductRepository(products = expected)
        val caseUse = ObserveProductsCaseUse(repository)

        val result = caseUse().first()

        assertEquals(expected, result)
    }

    @Test
    fun `sync products bubbles repository result`() = runTest {
        val repository = FakeProductRepository(syncResult = Result.success(Unit))
        val caseUse = SyncProductCaseUse(repository)

        val result = caseUse()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `get product by id returns product when repository has it`() = runTest {
        val expected = fakeProduct(id = "p-1")
        val repository = FakeProductRepository(products = listOf(expected))
        val caseUse = GetProductByIdCaseUse(repository)

        val result = caseUse("p-1")

        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `get product by id returns FormatException when repository has no product`() = runTest {
        val repository = FakeProductRepository(products = emptyList())
        val caseUse = GetProductByIdCaseUse(repository)

        val result = caseUse("p-missing")

        assertTrue(result.isFailure)
        assertIs<FormatException>(result.exceptionOrNull())
    }
}

private fun fakeProduct(id: String) = Product(
    id = id,
    name = "Product $id",
    description = "Description",
    price = 1.0,
    photoUrl = "https://test.local/$id.png",
    categoryId = "cat"
)
