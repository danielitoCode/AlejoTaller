package com.elitec.alejotaller.feature.exchange.data.mapper

import com.elitec.alejotaller.feature.exchange.data.dto.CupExchangeDTO
import com.elitec.alejotaller.feature.exchange.data.dto.CurrencyRateDTO
import org.junit.Assert.assertEquals
import org.junit.Test

class ExchangeMappersTest {

    @Test
    fun `toDomain should map DTO correctly`() {
        val dto = CupExchangeDTO(
            ok = true,
            fecha = "2024-06-14",
            hora = "10:00",
            actualizado = "2024-06-14T10:00:00Z",
            tasas = mapOf(
                "USD" to CurrencyRateDTO(CUP = 350f),
                "EUR" to CurrencyRateDTO(CUP = 360f)
            )
        )

        val domain = dto.toDomain()

        assertEquals(350f, domain.usdReference)
        assertEquals(360f, domain.euroReference)
        assertEquals("2024-06-14T10:00:00Z", domain.updatedAt)
        assertEquals("DIRECTORIO_CUBANO", domain.source)
    }

    @Test(expected = IllegalStateException::class)
    fun `toDomain should throw exception if USD rate is missing`() {
        val dto = CupExchangeDTO(
            ok = true,
            fecha = "2024-06-14",
            hora = "10:00",
            tasas = mapOf(
                "EUR" to CurrencyRateDTO(CUP = 360f)
            )
        )

        dto.toDomain()
    }
}
