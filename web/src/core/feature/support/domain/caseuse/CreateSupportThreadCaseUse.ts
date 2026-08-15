import type { SupportChatMessage, SupportReason, SupportThread } from "../entity/SupportMessage";
import type { SupportRepository } from "../repository/support.repository";

export type CreateSupportThreadInput = {
    userId: string;
    userName: string;
    userEmail: string;
    reason: SupportReason;
    subject: string;
    body: string;
};

export class CreateSupportThreadCaseUse {
    constructor(private readonly repo: SupportRepository) {}

    async execute(input: CreateSupportThreadInput): Promise<{ thread: SupportThread; firstMessage: SupportChatMessage }> {
        const subject = input.subject.trim();
        const body = input.body.trim();
        if (!subject) throw new Error("El asunto es obligatorio");
        if (!body) throw new Error("El mensaje es obligatorio");
        if (!input.userId) throw new Error("Debes iniciar sesión");

        const now = new Date().toISOString();
        const preview = body.length > 180 ? `${body.slice(0, 177)}…` : body;

        const thread = await this.repo.createThread({
            userId: input.userId,
            userName: input.userName || "Usuario",
            userEmail: input.userEmail || "",
            reason: input.reason,
            subject,
            status: "nuevo",
            lastMessageAt: now,
            lastPreview: preview,
            lastSenderRole: "user",
            unreadStaff: 1,
            unreadUser: 0
        });

        const firstMessage = await this.repo.postMessage({
            threadId: thread.id,
            senderRole: "user",
            senderId: input.userId,
            senderName: input.userName || "Usuario",
            body,
            createdAtIso: now
        });

        return { thread, firstMessage };
    }
}
