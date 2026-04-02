package com.elitec.shared.sale.feature.sale.domain.caseUse

import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
import com.elitec.shared.sale.feature.sale.fakes.FakeSaleRepository
import com.elitec.shared.sale.feature.sale.fakes.sampleSale
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class UpdateSaleVerificationFromRealtimeCaseUseTest {

    @Test
    fun `guarda VERIFIED cuando el realtime confirma la venta`() = runTest {
        val repository = FakeSaleRepository(sampleSale(verified = BuyState.UNVERIFIED))

        val result = UpdateSaleVerificationFromRealtimeCaseUse(repository)("sale-1", true)

        assertTrue(result.isSuccess)
        assertEquals(BuyState.VERIFIED, repository.currentSale.verified)
        assertEquals(1, repository.savedSales.size)
    }

    @Test
    fun `no guarda si la venta ya estaba en el estado esperado`() = runTest {
        val repository = FakeSaleRepository(sampleSale(verified = BuyState.DELETED))

        val result = UpdateSaleVerificationFromRealtimeCaseUse(repository)("sale-1", false)

        assertTrue(result.isSuccess)
        assertTrue(repository.savedSales.isEmpty())
    }
}
