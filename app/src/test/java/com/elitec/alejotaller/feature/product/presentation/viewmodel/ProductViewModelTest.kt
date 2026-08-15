package com.elitec.alejotaller.feature.product.presentation.viewmodel

import com.elitec.alejotaller.feature.product.domain.caseUse.ApplyProductRealtimeSnapshotsCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.GetProductByIdCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.ObserveProductsCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.RefreshProductsByIdsCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.SyncProductCaseUse
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.shared.core.feature.product.domain.realtime.StockUpdatesListener
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test

class ProductViewModelTest {

    @Test
    fun `return null when charged when product id is null`() = runTest {
        val observe = mockk<ObserveProductsCaseUse>()
        every { observe() } returns flowOf(emptyList())

        val getById = mockk<GetProductByIdCaseUse>()
        coEvery { getById(any()) } returns Result.success(null)

        val stockListener = mockk<StockUpdatesListener>(relaxed = true)
        every { stockListener.start(any()) } returns {}

        val viewModel = ProductViewModel(
            observeProductsCaseUse = observe,
            syncProductCaseUse = mockk(relaxed = true),
            getProductByIdCaseUse = getById,
            refreshProductsByIdsCaseUse = mockk(relaxed = true),
            applyProductRealtimeSnapshotsCaseUse = mockk(relaxed = true),
            stockUpdatesListener = stockListener
        )

        var productCharged: Product? = Product(
            id = "sentinel",
            name = "x",
            description = "x",
            price = 0.0,
            photoUrl = "",
            categoryId = "",
            existence = 0,
            reserved = 0
        )

        viewModel.getProductById("") { productCharged = it }
        advanceUntilIdle()

        assertNull(productCharged)
        viewModel.stopStockRealtime()
    }
}
