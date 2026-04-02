package com.elitec.alejotallerscan.feature.scan.domain.caseuse

import com.elitec.alejotallerscan.feature.scan.domain.entity.ParsedSaleQrItem
import com.elitec.alejotallerscan.feature.scan.domain.entity.ParsedSaleQrPayload
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class ParseSaleScanPayloadCaseUse {
    operator fun invoke(rawPayload: String): Result<ParsedSaleQrPayload> = runCatching {
        val trimmed = rawPayload.trim()
        require(trimmed.isNotBlank()) { "Debes escanear o pegar un codigo valido." }

        val normalized = runCatching {
            URLDecoder.decode(trimmed, StandardCharsets.UTF_8.name()).trim()
        }.getOrDefault(trimmed)

        val queryId = Regex("""(?:^|[?&])(id|saleId|reservationId)=([^&]+)""", RegexOption.IGNORE_CASE)
            .find(normalized)
            ?.groupValues
            ?.getOrNull(2)
            ?.trim()

        val jsonId = Regex(""""(?:id|saleId|reservationId)"\s*:\s*"([^"]+)"""", RegexOption.IGNORE_CASE)
            .find(normalized)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()

        val pipeSeparatedId = normalized
            .substringBefore('|')
            .trim()
            .takeIf { it.isNotBlank() && '|' in normalized }

        val candidate = when {
            !queryId.isNullOrBlank() -> queryId
            !jsonId.isNullOrBlank() -> jsonId
            !pipeSeparatedId.isNullOrBlank() -> pipeSeparatedId
            normalized.startsWith("http", ignoreCase = true) ->
                normalized.substringBefore('?').substringAfterLast('/').trim()
            else -> normalized.substringAfterLast('/').substringAfterLast('=').trim()
        }

        require(candidate.isNotBlank()) { "No se pudo extraer un codigo de venta valido." }

        val segments = normalized.split('|').map { it.trim() }
        val userId = segments.getOrNull(1)?.takeIf { it.isNotBlank() }
        val amount = segments.getOrNull(2)?.toDoubleOrNull()
        val items = segments.drop(3).mapNotNull { rawItem ->
            val parts = rawItem.split(':').map { it.trim() }
            val productId = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val quantity = parts.getOrNull(1)?.toIntOrNull() ?: 1
            val unitPrice = parts.getOrNull(2)?.toDoubleOrNull()
            ParsedSaleQrItem(
                productId = productId,
                quantity = quantity,
                unitPrice = unitPrice
            )
        }

        ParsedSaleQrPayload(
            saleId = candidate,
            userId = userId,
            amount = amount,
            items = items,
            rawPayload = rawPayload
        )
    }
}
