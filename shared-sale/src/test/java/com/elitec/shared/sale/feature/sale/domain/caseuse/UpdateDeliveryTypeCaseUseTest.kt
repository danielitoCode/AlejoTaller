package com.elitec.shared.sale.feature.sale.domain.caseuse

import com.elitec.shared.sale.feature.sale.domain.caseUse.UpdateDeliveryTypeCaseUse
import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
import com.elitec.shared.sale.feature.sale.domain.entity.DeliveryAddress
import com.elitec.shared.sale.feature.sale.domain.entity.DeliveryType
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.fakes.FakeSaleRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateDeliveryTypeCaseUseTest {

    private lateinit var repository: FakeSaleRepository
    private lateinit var updateSaleCaseUse: UpdateDeliveryTypeCaseUse

    private val targetSale = Sale(
        id = "sale tet",
        date = LocalDate(2026,2,13),
        amount = 23.4,
        verified = BuyState.VERIFIED,
        products = listOf(),
        userId = "user id test",
        customerName = "user name test",
        deliveryType = DeliveryType.DELIVERY,
        deliveryAddress = DeliveryAddress(
            province = "test",
            municipality = "test",
            mainStreet = "test",
            betweenStreets = "test",
            phone = "+55555555",
            houseNumber = "888s",
            referenceName = "khs"
        )
    )

    @Before
    fun `configure test`() = runTest {
        repository = FakeSaleRepository()
        repository.save(targetSale.copy(deliveryType = DeliveryType.PICKUP))

        updateSaleCaseUse = UpdateDeliveryTypeCaseUse(repository)
    }

    @Test
    fun `update a sale delivery type to a saved sale`() = runTest {
        // When
        val response = updateSaleCaseUse(targetSale.id, DeliveryType.DELIVERY)

        // Then
        assertEquals(Result.success(Unit), response)
        assertEquals(targetSale,repository.getById(targetSale.id))
    }

    @Test
    fun `try update a sale delivery to a saved sale`() = runTest {
        // When
        val response = updateSaleCaseUse("not existing sale id", DeliveryType.DELIVERY)

        // Then
        assert(response.isFailure)
    }
}