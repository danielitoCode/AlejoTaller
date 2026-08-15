import type { SupportChatMessageDTO, SupportThreadDTO } from "../dto/SupportMessageDTO";
import type {
    SupportChatMessage,
    SupportMessage,
    SupportReason,
    SupportSenderRole,
    SupportStatus,
    SupportThread
} from "../../domain/entity/SupportMessage";
import { threadToInboxRow } from "../../domain/entity/SupportMessage";

export function asReason(value: unknown): SupportReason {
    const v = String(value ?? "").toLowerCase();
    if (v === "soporte") return "soporte";
    if (v === "pregunta_tecnica") return "pregunta_tecnica";
    if (v === "facturacion") return "facturacion";
    return "otro";
}

export function asStatus(value: unknown): SupportStatus {
    const v = String(value ?? "").toLowerCase();
    if (v === "en_proceso") return "en_proceso";
    if (v === "resuelto") return "resuelto";
    if (v === "cerrado") return "cerrado";
    return "nuevo";
}

export function asSenderRole(value: unknown): SupportSenderRole {
    return String(value ?? "").toLowerCase() === "staff" ? "staff" : "user";
}

export function supportThreadFromDTO(dto: SupportThreadDTO): SupportThread {
    const created = dto.$createdAt ?? new Date(0).toISOString();
    return {
        id: dto.$id ?? dto.id ?? "",
        userId: dto.userId ?? "",
        userName: dto.userName ?? "",
        userEmail: dto.userEmail ?? "",
        reason: asReason(dto.reason),
        subject: dto.subject ?? "",
        status: asStatus(dto.status),
        lastMessageAt: dto.lastMessageAt ?? created,
        lastPreview: dto.lastPreview ?? "",
        lastSenderRole: asSenderRole(dto.lastSenderRole),
        unreadStaff: Number(dto.unreadStaff ?? 0),
        unreadUser: Number(dto.unreadUser ?? 0),
        createdAtIso: created
    };
}

export function supportChatMessageFromDTO(dto: SupportChatMessageDTO): SupportChatMessage {
    return {
        id: dto.$id ?? dto.id ?? "",
        threadId: dto.threadId ?? "",
        senderRole: asSenderRole(dto.senderRole),
        senderId: dto.senderId ?? "",
        senderName: dto.senderName ?? "",
        body: dto.body ?? "",
        createdAtIso: dto.createdAtIso ?? dto.$createdAt ?? new Date(0).toISOString()
    };
}

export function threadDtoToInboxRow(dto: SupportThreadDTO): SupportMessage {
    return threadToInboxRow(supportThreadFromDTO(dto));
}

export type SupportThreadWritePayload = {
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
};

export type SupportChatMessageWritePayload = {
    threadId: string;
    senderRole: SupportSenderRole;
    senderId: string;
    senderName: string;
    body: string;
    createdAtIso: string;
};
