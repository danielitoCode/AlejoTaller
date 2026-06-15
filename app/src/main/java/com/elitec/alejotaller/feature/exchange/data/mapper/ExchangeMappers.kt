package com.elitec.alejotaller.feature.exchange.data.mapper

import com.elitec.alejotaller.feature.exchange.data.dto.CupExchangeDTO
import com.elitec.alejotaller.feature.exchange.data.dto.CupExchangeLocalDto
import com.elitec.alejotaller.feature.exchange.domain.entity.CupExchange
import kotlin.time.Clock

fun CupExchangeDTO.toDomain(): CupExchange {
    val usdReference = tasas["USD"]?.CUP
    val euroReference = tasas["EUR"]?.CUP

    if (usdReference == null || euroReference == null) {
        throw IllegalStateException("No se pudieron obtener las tasas USD/CUP y EUR/CUP.")
    }

    val updatedAtValue = actualizado ?: Clock.System.now().toString()

    return CupExchange(
        id = "directorioCubano-${updatedAtValue.take(10)}",
        usdReference = usdReference,
        euroReference = euroReference,
        updatedAt = updatedAtValue,
        source = "DIRECTORIO_CUBANO"
    )
}

fun CupExchange.toLocalDto(): CupExchangeLocalDto {
    return CupExchangeLocalDto(
        id = id,
        usdReference = usdReference,
        euroReference = euroReference,
        updatedAt = updatedAt,
        source = source
    )
}

fun CupExchangeLocalDto.toDomain(): CupExchange {
    return CupExchange(
        id = id,
        usdReference = usdReference,
        euroReference = euroReference,
        updatedAt = updatedAt,
        source = source
    )
}
