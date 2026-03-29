package com.elitec.alejotaller.feature.sale.data.repository

import com.elitec.alejotaller.feature.sale.data.dto.SaleDto
import com.elitec.alejotaller.feature.sale.domain.entity.SaleItem
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.datetime.LocalDate
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SaleAppwritePayloadMapperTest {

    @Test
    fun `toAppwriteData maps sale to backend payload using expected keys`() {
        val dto = SaleDto(
            id = "sale-1",
            date = LocalDate(2026, 3, 21),
            amount = 150.0,
            verified = "UNVERIFIED",
            products = listOf(SaleItem(productId = "prod-1", quantity = 2)),
            userId = "user-1",
            deliveryType = "PICKUP",
            deliveryAddress = """{"province":"La Habana","municipality":"Playa","mainStreet":"42","phone":"555","houseNumber":"12"}"""
        )

        val payload = dto.toAppwriteData()

        assertFalse(payload.containsKey("id"))
        assertEquals("2026-03-21", payload["date"])
        assertEquals(150.0, payload["amount"])
        assertEquals("UNVERIFIED", payload["verified"])
        assertEquals("user-1", payload["user_id"])
        assertEquals("PICKUP", payload["delivery_type"])
        assertTrue((payload["delivery_address"] as? String)?.contains("\"province\":\"La Habana\"") == true)

        val products = payload["products"] as? List<*>
        assertTrue(products?.isNotEmpty() == true)
        val firstProduct = products?.firstOrNull() as? Map<*, *>
        assertEquals("prod-1", firstProduct?.get("productId"))
        assertEquals(2, firstProduct?.get("quantity"))
    }

    @Test
    fun `toAppwriteData omits delivery_type when null`() {
        val dto = SaleDto(
            id = "sale-1",
            date = LocalDate(2026, 3, 21),
            amount = 150.0,
            verified = "UNVERIFIED",
            products = emptyList(),
            userId = "user-1",
            deliveryType = null,
            deliveryAddress = null
        )

        val payload = dto.toAppwriteData()

        assertFalse(payload.containsKey("delivery_type"))
        assertNull(payload["delivery_type"])
        assertFalse(payload.containsKey("delivery_address"))
        assertNull(payload["delivery_address"])
    }
}
