package com.elitec.alejotaller.feature.exchange.data.repository

import com.elitec.alejotaller.feature.exchange.data.dao.ExchangeDao
import com.elitec.alejotaller.feature.exchange.data.dto.CupExchangeDTO
import com.elitec.alejotaller.feature.exchange.data.dto.CupExchangeLocalDto
import com.elitec.alejotaller.feature.exchange.data.dto.CurrencyRateDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ExchangeOfflineFirstRepositoryTest {

    private val net: ExchangeNetRepository = mockk()
    private val dao: ExchangeDao = mockk()
    private val repository = ExchangeOfflineFirstRepository(net, dao)

    @Test
    fun `getToday should fetch from net and save to dao`() = runBlocking {
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

        coEvery { net.getExchangeToday() } returns dto
        coEvery { dao.insertExchange(any()) } returns Unit

        val result = repository.getToday()

        assertEquals(350f, result.usdReference)
        coVerify { net.getExchangeToday() }
        coVerify { dao.insertExchange(match { it.id == "directorioCubano-today" && it.usdReference == 350f }) }
    }

    @Test
    fun `getToday should fallback to cache when net fails`() = runBlocking {
        val cached = CupExchangeLocalDto(
            id = "directorioCubano-today",
            usdReference = 340f,
            euroReference = 350f,
            updatedAt = "2024-06-13T10:00:00Z",
            source = "DIRECTORIO_CUBANO"
        )

        coEvery { net.getExchangeToday() } throws Exception("Network error")
        coEvery { dao.getExchangeById("directorioCubano-today") } returns cached

        val result = repository.getToday()

        assertEquals(340f, result.usdReference)
        coVerify { dao.getExchangeById("directorioCubano-today") }
    }
}
