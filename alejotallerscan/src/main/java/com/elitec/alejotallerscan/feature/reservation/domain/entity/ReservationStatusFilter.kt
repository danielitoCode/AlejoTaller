package com.elitec.alejotallerscan.feature.reservation.domain.entity

import com.elitec.shared.sale.feature.sale.domain.entity.BuyState

enum class ReservationStatusFilter(val state: BuyState?) {
    ALL(null),
    PENDING(BuyState.UNVERIFIED),
    CONFIRMED(BuyState.VERIFIED),
    REJECTED(BuyState.DELETED)
}
