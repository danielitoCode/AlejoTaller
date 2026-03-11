package com.elitec.alejotaller.feature.sale.data.repository

import com.elitec.alejotaller.feature.sale.data.dto.SaleDto
import com.elitec.alejotaller.feature.sale.domain.entity.SaleItem
import kotlinx.datetime.LocalDate
import org.junit.Test
import junit.framework.TestCase.assertEquals

class SaleSyncReconciliationTest {

    @Test
    fun resolvePendingLocals_returns_only_local_sales_missing_remotely() {
        val localSales = listOf(
            fakeSale(id = "sale-1"),
            fakeSale(id = "sale-2"),
            fakeSale(id = "sale-3")
        )
        val remoteSales = listOf(
            fakeSale(id = "sale-1"),
            fakeSale(id = "sale-3")
        )

        val pending = resolvePendingLocals(
            localSales = localSales,
            remoteSales = remoteSales
        )

        assertEquals(listOf(fakeSale(id = "sale-2")), pending)
    }

    @Test
    fun resolvePendingLocals_returns_empty_when_all_local_sales_already_exist_remotely() {
        val localSales = listOf(fakeSale(id = "sale-1"), fakeSale(id = "sale-2"))
        val remoteSales = listOf(fakeSale(id = "sale-1"), fakeSale(id = "sale-2"))

        val pending = resolvePendingLocals(
            localSales = localSales,
            remoteSales = remoteSales
        )

        assertEquals(emptyList<SaleDto>(), pending)
    }

    private fun fakeSale(id: String) = SaleDto(
        id = id,
        date = LocalDate(2026, 1, 1),
        amount = 100.0,
        verified = "UNVERIFIED",
        products = listOf(SaleItem("p1", 1,"test")),
        userId = "u-1"
    )
}