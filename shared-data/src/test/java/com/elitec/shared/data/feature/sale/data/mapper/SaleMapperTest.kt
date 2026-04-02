package com.elitec.shared.data.feature.sale.data.mapper

import com.elitec.shared.data.feature.sale.data.dto.SaleDto
import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
import com.elitec.shared.sale.feature.sale.domain.entity.DeliveryAddress
import com.elitec.shared.sale.feature.sale.domain.entity.DeliveryType
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.entity.SaleItem
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

class SaleMapperTest {

    @Test
    fun `toDomain decodifica delivery address valido`() {
        val address = DeliveryAddress(
            province = "La Habana",
            municipality = "Playa",
            mainStreet = "42",
            betweenStreets = "5ta y 7ma",
            phone = "+5350000000",
            houseNumber = "101",
            referenceName = "Carlos"
        )
        val dto = SaleDto(
            id = "sale-1",
            date = LocalDate(2026, 4, 2),
            amount = 10.0,
            verified = "VERIFIED",
            products = listOf(SaleItem(productId = "p1", quantity = 1, productName = "Battery")),
            userId = "user-1",
            customerName = "Carlos",
            deliveryType = "DELIVERY",
            deliveryAddress = Json.encodeToString(address)
        )

        val domain = dto.toDomain()

        assertEquals(BuyState.VERIFIED, domain.verified)
        assertEquals(DeliveryType.DELIVERY, domain.deliveryType)
        assertEquals(address, domain.deliveryAddress)
    }

    @Test
    fun `toDomain deja delivery address en null si el json es invalido`() {
        val dto = SaleDto(
            id = "sale-1",
            date = LocalDate(2026, 4, 2),
            amount = 10.0,
            verified = "UNVERIFIED",
            products = listOf(SaleItem(productId = "p1", quantity = 1, productName = "Battery")),
            userId = "user-1",
            deliveryType = "DELIVERY",
            deliveryAddress = "{bad-json"
        )

        val domain = dto.toDomain()

        assertNull(domain.deliveryAddress)
    }

    @Test
    fun `toDto serializa delivery address cuando existe`() {
        val sale = Sale(
            id = "sale-1",
            date = LocalDate(2026, 4, 2),
            amount = 10.0,
            verified = BuyState.UNVERIFIED,
            products = listOf(SaleItem(productId = "p1", quantity = 1, productName = "Battery")),
            userId = "user-1",
            customerName = "Carlos",
            deliveryType = DeliveryType.DELIVERY,
            deliveryAddress = DeliveryAddress(
                province = "La Habana",
                municipality = "Playa",
                mainStreet = "42",
                phone = "+5350000000",
                houseNumber = "101"
            )
        )

        val dto = sale.toDto()

        assertEquals("UNVERIFIED", dto.verified)
        assertEquals("DELIVERY", dto.deliveryType)
        assertEquals("Carlos", dto.customerName)
        assertEquals(sale.deliveryAddress, dto.deliveryAddress?.let { Json.decodeFromString<DeliveryAddress>(it) })
    }
}
