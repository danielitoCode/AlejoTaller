package com.elitec.alejotaller.feature.product.presentation.viewmodel

import com.elitec.alejotaller.feature.product.domain.entity.Product
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertNull

class ProductViewModelTest {
    private val productViewModel: ProductViewModel = mockk()

    @Test
    fun `return null when charged when product id is null` () = runTest {
        var productCharged: Product? = null
        val productProcedureWhenIsCharged = { product: Product? ->
            productCharged = product
        }

        productViewModel.getProductById("", productProcedureWhenIsCharged)

        assertNull(productCharged)
    }
}