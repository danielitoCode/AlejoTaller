import type { SupportChatMessage, SupportSenderRole, SupportStatus } from "../entity/SupportMessage";
import type { SupportRepository } from "../repository/support.repository";

function clampUnread(n: number): number {
    if (!Number.isFinite(n) || n < 0) return 0;
    return Math.min(Math.floor(n), 99);
}

/**
 * Publica un mensaje y actualiza last* / unread del hilo.
 * Contrato alineado con panel:
 * - user → incrementa unreadStaff (mín. 1)
 * - staff → unreadUser ≥ 1, unreadStaff = 0, status en_proceso
 * Si el patch del hilo falla, el mensaje ya quedó persistido (no se revierte).
 */
export class PostSupportMessageCaseUse {
    constructor(private readonly repo: SupportRepository) {}

    async execute(input: {
        threadId: string;
        senderRole: SupportSenderRole;
        senderId: string;
        senderName: string;
        body: string;
        nextStatus?: SupportStatus;
    }): Promise<SupportChatMessage> {
        const body = input.body.trim();
        const threadId = input.threadId?.trim();
        if (!body) throw new Error("El mensaje no puede estar vacío");
        if (!threadId) throw new Error("threadId requerido");
        if (!input.senderId?.trim()) throw new Error("senderId requerido");

        const now = new Date().toISOString();
        const message = await this.repo.postMessage({
            threadId,
            senderRole: input.senderRole,
            senderId: input.senderId,
            senderName: input.senderName || (input.senderRole === "staff" ? "Soporte" : "Usuario"),
            body,
            createdAtIso: now
        });

        const preview = body.length > 180 ? `${body.slice(0, 177)}…` : body;

        // Contadores: leer estado actual para incrementar; si falla get → fallback a 1
        let unreadStaff = 1;
        let unreadUser = 0;
        let status: SupportStatus | undefined;

        try {
            const current = await this.repo.getThread(threadId);
            if (input.senderRole === "user") {
                unreadStaff = clampUnread((current?.unreadStaff ?? 0) + 1) || 1;
                // El usuario está escribiendo: no tocar unreadUser (o dejarlo)
                unreadUser = clampUnread(current?.unreadUser ?? 0);
            } else {
                unreadUser = clampUnread((current?.unreadUser ?? 0) + 1) || 1;
                unreadStaff = 0;
                status = input.nextStatus ?? "en_proceso";
            }
        } catch {
            if (input.senderRole === "user") {
                unreadStaff = 1;
            } else {
                unreadUser = 1;
                unreadStaff = 0;
                status = input.nextStatus ?? "en_proceso";
            }
        }

        try {
            await this.repo.touchThread(threadId, {
                lastMessageAt: now,
                lastPreview: preview,
                lastSenderRole: input.senderRole,
                unreadStaff,
                unreadUser,
                ...(status ? { status } : {})
            });
        } catch {
            // Soft-fail: mensaje ya enviado; el panel/cliente verán el mensaje vía listMessages
            // aunque last*/unread queden desfasados hasta el próximo touch exitoso.
        }

        return message;
    }
}
