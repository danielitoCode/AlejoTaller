package com.elitec.alejotallerscan.feature.scan.presentation.entity

enum class QrConsistencyStatus {
    MATCH,
    MISMATCH,
    UNKNOWN
}

data class QrConsistencyCheck(
    val label: String,
    val qrValue: String,
    val appwriteValue: String,
    val status: QrConsistencyStatus
)
