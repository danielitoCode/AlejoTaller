package com.elitec.alejotaller.feature.exchange.presentation.viewmodel

import com.elitec.alejotaller.core.MainDispatcherRule
import com.elitec.alejotaller.feature.exchange.domain.caseUse.GetCachedTodayExchangeCaseUse
import com.elitec.alejotaller.feature.exchange.domain.caseUse.GetTodayExchangeCaseUse
import com.elitec.alejotaller.feature.exchange.domain.entity.CupExchange
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class ExchangeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTodayExchangeCaseUse: GetTodayExchangeCaseUse = mockk()
    private val getCachedTodayExchangeCaseUse: GetCachedTodayExchangeCaseUse = mockk()

    private fun createViewModel(): ExchangeViewModel {
        return ExchangeViewModel(getTodayExchangeCaseUse, getCachedTodayExchangeCaseUse)
    }

    @Test
    fun `init should hydrate from cache if valid`() = runTest {
        val now = java.time.LocalDate.now().toString()
        val cached = CupExchange("id", 350f, 360f, now)
        coEvery { getCachedTodayExchangeCaseUse() } returns cached

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(cached, viewModel.uiState.value.exchange)
    }

    @Test
    fun `init should refresh from net if cache is missing`() = runTest {
        val remote = CupExchange("id", 350f, 360f, "some-date")
        coEvery { getCachedTodayExchangeCaseUse() } returns null
        coEvery { getTodayExchangeCaseUse() } returns Result.success(remote)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(remote, viewModel.uiState.value.exchange)
    }

    @Test
    fun `refresh should update state on success`() = runTest {
        val remote = CupExchange("id", 350f, 360f, "some-date")
        coEvery { getCachedTodayExchangeCaseUse() } returns null
        coEvery { getTodayExchangeCaseUse() } returns Result.success(remote)

        val viewModel = createViewModel()
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(remote, viewModel.uiState.value.exchange)
        assertFalse(viewModel.uiState.value.loading)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun `refresh should update error on failure`() = runTest {
        coEvery { getCachedTodayExchangeCaseUse() } returns null
        coEvery { getTodayExchangeCaseUse() } returns Result.failure(Exception("Error message"))

        val viewModel = createViewModel()
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(viewModel.uiState.value.exchange, null)
        assertFalse(viewModel.uiState.value.loading)
        assertEquals("Error message", viewModel.uiState.value.error)
    }

    @Test
    fun `toggleCurrency should switch between CUP and USD`() = runTest {
        coEvery { getCachedTodayExchangeCaseUse() } returns null
        coEvery { getTodayExchangeCaseUse() } returns Result.success(mockk())
        
        val viewModel = createViewModel()
        assertEquals("CUP", viewModel.uiState.value.selectedCurrency)

        viewModel.toggleCurrency()
        assertEquals("USD", viewModel.uiState.value.selectedCurrency)

        viewModel.toggleCurrency()
        assertEquals("CUP", viewModel.uiState.value.selectedCurrency)
    }

    @Test
    fun `setCurrency should update selected currency`() = runTest {
        coEvery { getCachedTodayExchangeCaseUse() } returns null
        coEvery { getTodayExchangeCaseUse() } returns Result.success(mockk())

        val viewModel = createViewModel()
        viewModel.setCurrency("USD")
        assertEquals("USD", viewModel.uiState.value.selectedCurrency)
    }
}
