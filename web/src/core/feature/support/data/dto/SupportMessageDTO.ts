export interface SupportThreadDTO {
    $id?: string;
    id?: string;
    $createdAt?: string;
    $updatedAt?: string;
    userId?: string;
    userName?: string;
    userEmail?: string;
    reason?: string;
    subject?: string;
    status?: string;
    lastMessageAt?: string;
    lastPreview?: string;
    lastSenderRole?: string;
    unreadStaff?: number;
    unreadUser?: number;
}

export interface SupportChatMessageDTO {
    $id?: string;
    id?: string;
    $createdAt?: string;
    threadId?: string;
    senderRole?: string;
    senderId?: string;
    senderName?: string;
    body?: string;
    createdAtIso?: string;
}
