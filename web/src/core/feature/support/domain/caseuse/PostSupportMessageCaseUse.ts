import type { SupportChatMessage, SupportSenderRole } from "../entity/SupportMessage";
import type { SupportRepository } from "../repository/support.repository";

export class PostSupportMessageCaseUse {
    constructor(private readonly repo: SupportRepository) {}

    async execute(input: {
        threadId: string;
        senderRole: SupportSenderRole;
        senderId: string;
        senderName: string;
        body: string;
    }): Promise<SupportChatMessage> {
        const body = input.body.trim();
        if (!body) throw new Error("El mensaje no puede estar vacío");
        if (!input.threadId?.trim()) throw new Error("threadId requerido");

        const now = new Date().toISOString();
        const message = await this.repo.postMessage({
            threadId: input.threadId,
            senderRole: input.senderRole,
            senderId: input.senderId,
            senderName: input.senderName,
            body,
            createdAtIso: now
        });

        const preview = body.length > 180 ? `${body.slice(0, 177)}…` : body;
        try {
            await this.repo.touchThread(input.threadId, {
                lastMessageAt: now,
                lastPreview: preview,
                lastSenderRole: input.senderRole,
                unreadStaff: input.senderRole === "user" ? 1 : undefined,
                unreadUser: input.senderRole === "staff" ? 1 : undefined
            });
        } catch {
            // Mensaje ya persistido; no fallar el envío si el patch del hilo falla
            // (permisos Update, red, etc.)
        }

        return message;
    }
}
