package com.elitec.alejotaller.feature.support.domain.repository

import com.elitec.alejotaller.feature.support.domain.entity.SupportChatMessage
import com.elitec.alejotaller.feature.support.domain.entity.SupportInboxRow
import com.elitec.alejotaller.feature.support.domain.entity.SupportSenderRole
import com.elitec.alejotaller.feature.support.domain.entity.SupportStatus
import com.elitec.alejotaller.feature.support.domain.entity.SupportThread

data class SupportRealtimeEvent(
    val events: List<String>,
    val target: Target,
    val payload: Map<String, Any?>? = null,
) {
    enum class Target { Threads, Messages, Unknown }
}

data class CreateThreadPayload(
    val userId: String,
    val userName: String,
    val userEmail: String,
    val reasonWire: String,
    val subject: String,
    val status: SupportStatus,
    val lastMessageAt: String,
    val lastPreview: String,
    val lastSenderRole: SupportSenderRole,
    val unreadStaff: Int,
    val unreadUser: Int,
)

data class PostMessagePayload(
    val threadId: String,
    val senderRole: SupportSenderRole,
    val senderId: String,
    val senderName: String,
    val body: String,
    val createdAtIso: String,
)

data class TouchThreadPatch(
    val status: SupportStatus? = null,
    val lastMessageAt: String? = null,
    val lastPreview: String? = null,
    val lastSenderRole: SupportSenderRole? = null,
    val unreadStaff: Int? = null,
    val unreadUser: Int? = null,
)

interface SupportRepository {
    suspend fun listMyThreads(userId: String): List<SupportInboxRow>
    suspend fun getThread(id: String): SupportThread?
    suspend fun listMessages(threadId: String): List<SupportChatMessage>
    suspend fun createThread(payload: CreateThreadPayload, documentId: String? = null): SupportThread
    suspend fun postMessage(payload: PostMessagePayload, documentId: String? = null): SupportChatMessage
    suspend fun touchThread(id: String, patch: TouchThreadPatch)
    /** Subscribe Appwrite RT; returns unsubscribe. */
    fun subscribe(handler: (SupportRealtimeEvent) -> Unit): () -> Unit
}
