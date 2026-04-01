package com.elitec.alejotallerscan.feature.reservation.domain.caseuse

import com.elitec.alejotallerscan.feature.reservation.domain.entity.ReservationSearchField
import com.elitec.shared.data.feature.sale.data.repository.SaleNetRepository
import com.elitec.shared.data.feature.sale.data.mapper.toDomain
import com.elitec.shared.sale.feature.sale.domain.entity.Sale

class SearchReservationsCaseUse(
    private val saleNetRepository: SaleNetRepository
) {
    suspend operator fun invoke(
        field: ReservationSearchField,
        query: String
    ): Result<List<Sale>> = runCatching {
        saleNetRepository.search(field.name, query.trim()).map { it.toDomain() }
    }
}
