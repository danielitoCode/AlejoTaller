package com.elitec.alejotallerscan.feature.scan.domain.caseuse

class ParseSaleScanPayloadCaseUse {
    operator fun invoke(rawPayload: String): Result<String> = runCatching {
        val trimmed = rawPayload.trim()
        require(trimmed.isNotBlank()) { "Debes escanear o pegar un codigo valido." }

        val queryId = Regex("""(?:^|[?&])(id|saleId|reservationId)=([^&]+)""", RegexOption.IGNORE_CASE)
            .find(trimmed)
            ?.groupValues
            ?.getOrNull(2)
            ?.trim()

        val candidate = when {
            !queryId.isNullOrBlank() -> queryId
            else -> trimmed.substringAfterLast('/').substringAfterLast('=').trim()
        }

        require(candidate.isNotBlank()) { "No se pudo extraer un codigo de venta valido." }
        candidate
    }
}
