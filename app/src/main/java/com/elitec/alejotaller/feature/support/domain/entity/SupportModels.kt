package com.elitec.alejotaller.feature.support.domain.entity

/**
 * Paridad con web + panel (Appwrite support_threads / support_messages).
 */
enum class SupportReason(val wire: String) {
    Soporte("soporte"),
    PreguntaTecnica("pregunta_tecnica"),
    Facturacion("facturacion"),
    Otro("otro");

    companion object {
        fun fromWire(value: String?): SupportReason {
            val v = value?.trim()?.lowercase().orEmpty()
            return entries.firstOrNull { it.wire == v } ?: Otro
        }
    }
}

enum class SupportStatus(val wire: String) {
    Nuevo("nuevo"),
    EnProceso("en_proceso"),
    Resuelto("resuelto"),
    Cerrado("cerrado");

    companion object {
        fun fromWire(value: String?): SupportStatus {
            val v = value?.trim()?.lowercase().orEmpty()
            return entries.firstOrNull { it.wire == v } ?: Nuevo
        }
    }

    fun labelEs(): String = when (this) {
        Nuevo -> "Nuevo"
        EnProceso -> "En proceso"
        Resuelto -> "Resuelto"
        Cerrado -> "Cerrado"
    }
}

enum class SupportSenderRole(val wire: String) {
    User("user"),
    Staff("staff");

    companion object {
        fun fromWire(value: String?): SupportSenderRole {
            val v = value?.trim()?.lowercase().orEmpty()
            return if (v == "staff") Staff else User
        }
    }
}

data class SupportThread(
    val id: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val reason: SupportReason,
    val subject: String,
    val status: SupportStatus,
    val lastMessageAt: String,
    val lastPreview: String,
    val lastSenderRole: SupportSenderRole,
    val unreadStaff: Int,
    val unreadUser: Int,
    val createdAtIso: String,
)

/** Fila de lista “Mis consultas” (paridad web SupportMessage inbox row). */
data class SupportInboxRow(
    val id: String,
    val createdAtIso: String,
    val fromName: String,
    val fromEmail: String,
    val reason: SupportReason,
    val status: SupportStatus,
    val subject: String,
    val body: String,
    val userId: String? = null,
    val unreadUser: Int = 0,
    val lastSenderRole: SupportSenderRole? = null,
)

data class SupportChatMessage(
    val id: String,
    val threadId: String,
    val senderRole: SupportSenderRole,
    val senderId: String,
    val senderName: String,
    val body: String,
    val createdAtIso: String,
)

fun SupportThread.toInboxRow(): SupportInboxRow = SupportInboxRow(
    id = id,
    createdAtIso = lastMessageAt.ifBlank { createdAtIso },
    fromName = userName,
    fromEmail = userEmail,
    reason = reason,
    status = status,
    subject = subject,
    body = lastPreview,
    userId = userId,
    unreadUser = unreadUser,
    lastSenderRole = lastSenderRole,
)

internal fun clampUnread(n: Int): Int = when {
    n < 0 -> 0
    n > 99 -> 99
    else -> n
}
