package com.elitec.alejotallerscan.feature.reservation.presentation.viewmodel

import com.elitec.alejotallerscan.feature.reservation.domain.entity.ReservationSearchField
import com.elitec.alejotallerscan.feature.reservation.domain.entity.ReservationStatusFilter
import com.elitec.shared.sale.feature.sale.domain.entity.Sale

data class OperatorReservationSearchUiState(
    val query: String = "",
    val field: ReservationSearchField = ReservationSearchField.SALE_ID,
    val statusFilter: ReservationStatusFilter = ReservationStatusFilter.ALL,
    val isLoading: Boolean = false,
    val results: List<Sale> = emptyList(),
    val error: String? = null,
    val notice: String? = null
)
