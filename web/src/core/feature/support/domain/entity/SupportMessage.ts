export type SupportReason = "soporte" | "pregunta_tecnica" | "facturacion" | "otro";
export type SupportStatus = "nuevo" | "en_proceso" | "resuelto" | "cerrado";
export type SupportSenderRole = "user" | "staff";

export interface SupportThread {
    id: string;
    userId: string;
    userName: string;
    userEmail: string;
    reason: SupportReason;
    subject: string;
    status: SupportStatus;
    lastMessageAt: string;
    lastPreview: string;
    lastSenderRole: SupportSenderRole;
    unreadStaff: number;
    unreadUser: number;
    createdAtIso: string;
}

export interface SupportChatMessage {
    id: string;
    threadId: string;
    senderRole: SupportSenderRole;
    senderId: string;
    senderName: string;
    body: string;
    createdAtIso: string;
}

/** Fila de lista “Mis consultas”. */
export interface SupportMessage {
    id: string;
    createdAtIso: string;
    fromName: string;
    fromEmail: string;
    reason: SupportReason;
    status: SupportStatus;
    subject: string;
    body: string;
    userId?: string;
    unreadUser?: number;
    lastSenderRole?: SupportSenderRole;
}

export function threadToInboxRow(thread: SupportThread): SupportMessage {
    return {
        id: thread.id,
        createdAtIso: thread.lastMessageAt || thread.createdAtIso,
        fromName: thread.userName,
        fromEmail: thread.userEmail,
        reason: thread.reason,
        status: thread.status,
        subject: thread.subject,
        body: thread.lastPreview,
        userId: thread.userId,
        unreadUser: thread.unreadUser,
        lastSenderRole: thread.lastSenderRole
    };
}
