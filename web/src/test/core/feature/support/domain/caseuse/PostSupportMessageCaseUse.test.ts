/**
 * @file PostSupportMessageCaseUse.test.ts
 * @description Unit tests for unread counter logic when posting support messages.
 * Contract (aligned with panel):
 * - user → increment unreadStaff (min 1), leave unreadUser as-is
 * - staff → increment unreadUser (min 1), unreadStaff = 0, status en_proceso
 * Soft-fail on touchThread after successful postMessage.
 */

import { describe, it, expect, beforeEach, vi } from "vitest";
import { PostSupportMessageCaseUse } from "../../../../../../core/feature/support/domain/caseuse/PostSupportMessageCaseUse";
import type { SupportRepository } from "../../../../../../core/feature/support/domain/repository/support.repository";
import type { SupportChatMessage, SupportThread } from "../../../../../../core/feature/support/domain/entity/SupportMessage";

function makeMessage(overrides: Partial<SupportChatMessage> = {}): SupportChatMessage {
    return {
        id: "msg-1",
        threadId: "thread-1",
        senderRole: "user",
        senderId: "user-1",
        senderName: "Usuario",
        body: "Hola soporte",
        createdAtIso: "2026-08-15T12:00:00.000Z",
        ...overrides
    };
}

function makeThread(overrides: Partial<SupportThread> = {}): SupportThread {
    return {
        id: "thread-1",
        userId: "user-1",
        userName: "Usuario",
        userEmail: "u@example.com",
        reason: "soporte",
        subject: "Consulta",
        status: "nuevo",
        lastMessageAt: "2026-08-15T11:00:00.000Z",
        lastPreview: "prev",
        lastSenderRole: "user",
        unreadStaff: 0,
        unreadUser: 0,
        createdAtIso: "2026-08-15T10:00:00.000Z",
        ...overrides
    };
}

describe("PostSupportMessageCaseUse — contadores unread", () => {
    let repo: {
        postMessage: ReturnType<typeof vi.fn>;
        getThread: ReturnType<typeof vi.fn>;
        touchThread: ReturnType<typeof vi.fn>;
    } & Partial<SupportRepository>;
    let useCase: PostSupportMessageCaseUse;

    beforeEach(() => {
        repo = {
            postMessage: vi.fn(),
            getThread: vi.fn(),
            touchThread: vi.fn().mockResolvedValue(undefined)
        };
        useCase = new PostSupportMessageCaseUse(repo as unknown as SupportRepository);
        vi.clearAllMocks();
    });

    describe("validación de entrada", () => {
        it("rechaza body vacío", async () => {
            await expect(
                useCase.execute({
                    threadId: "t1",
                    senderRole: "user",
                    senderId: "u1",
                    senderName: "U",
                    body: "   "
                })
            ).rejects.toThrow("El mensaje no puede estar vacío");
            expect(repo.postMessage).not.toHaveBeenCalled();
        });

        it("rechaza threadId vacío", async () => {
            await expect(
                useCase.execute({
                    threadId: "  ",
                    senderRole: "user",
                    senderId: "u1",
                    senderName: "U",
                    body: "hola"
                })
            ).rejects.toThrow("threadId requerido");
        });

        it("rechaza senderId vacío", async () => {
            await expect(
                useCase.execute({
                    threadId: "t1",
                    senderRole: "user",
                    senderId: "  ",
                    senderName: "U",
                    body: "hola"
                })
            ).rejects.toThrow("senderId requerido");
        });
    });

    describe("cuando el usuario responde", () => {
        it("incrementa unreadStaff a partir del valor actual", async () => {
            const msg = makeMessage();
            repo.postMessage.mockResolvedValue(msg);
            repo.getThread.mockResolvedValue(makeThread({ unreadStaff: 2, unreadUser: 1 }));

            await useCase.execute({
                threadId: "thread-1",
                senderRole: "user",
                senderId: "user-1",
                senderName: "Usuario",
                body: "Hola soporte"
            });

            expect(repo.touchThread).toHaveBeenCalledWith(
                "thread-1",
                expect.objectContaining({
                    unreadStaff: 3,
                    unreadUser: 1,
                    lastSenderRole: "user",
                    lastPreview: "Hola soporte"
                })
            );
            // No debe forzar status en respuesta de usuario
            const patch = repo.touchThread.mock.calls[0][1];
            expect(patch).not.toHaveProperty("status");
        });

        it("usa mínimo 1 cuando unreadStaff actual es 0", async () => {
            repo.postMessage.mockResolvedValue(makeMessage());
            repo.getThread.mockResolvedValue(makeThread({ unreadStaff: 0 }));

            await useCase.execute({
                threadId: "thread-1",
                senderRole: "user",
                senderId: "user-1",
                senderName: "Usuario",
                body: "primer mensaje"
            });

            expect(repo.touchThread).toHaveBeenCalledWith(
                "thread-1",
                expect.objectContaining({ unreadStaff: 1, unreadUser: 0 })
            );
        });

        it("clampa unreadStaff a 99", async () => {
            repo.postMessage.mockResolvedValue(makeMessage());
            repo.getThread.mockResolvedValue(makeThread({ unreadStaff: 99 }));

            await useCase.execute({
                threadId: "thread-1",
                senderRole: "user",
                senderId: "user-1",
                senderName: "Usuario",
                body: "otro"
            });

            expect(repo.touchThread).toHaveBeenCalledWith(
                "thread-1",
                expect.objectContaining({ unreadStaff: 99 })
            );
        });

        it("fallback unreadStaff=1 si getThread falla", async () => {
            repo.postMessage.mockResolvedValue(makeMessage());
            repo.getThread.mockRejectedValue(new Error("network"));

            await useCase.execute({
                threadId: "thread-1",
                senderRole: "user",
                senderId: "user-1",
                senderName: "Usuario",
                body: "hola"
            });

            expect(repo.touchThread).toHaveBeenCalledWith(
                "thread-1",
                expect.objectContaining({ unreadStaff: 1, unreadUser: 0 })
            );
        });

        it("fallback unreadStaff=1 si getThread devuelve null", async () => {
            repo.postMessage.mockResolvedValue(makeMessage());
            repo.getThread.mockResolvedValue(null);

            await useCase.execute({
                threadId: "thread-1",
                senderRole: "user",
                senderId: "user-1",
                senderName: "Usuario",
                body: "hola"
            });

            expect(repo.touchThread).toHaveBeenCalledWith(
                "thread-1",
                expect.objectContaining({ unreadStaff: 1, unreadUser: 0 })
            );
        });
    });

    describe("cuando staff responde", () => {
        it("incrementa unreadUser, pone unreadStaff=0 y status en_proceso", async () => {
            const msg = makeMessage({ senderRole: "staff", senderName: "Soporte" });
            repo.postMessage.mockResolvedValue(msg);
            repo.getThread.mockResolvedValue(makeThread({ unreadUser: 0, unreadStaff: 4 }));

            await useCase.execute({
                threadId: "thread-1",
                senderRole: "staff",
                senderId: "staff-1",
                senderName: "Soporte",
                body: "Respuesta del panel"
            });

            expect(repo.touchThread).toHaveBeenCalledWith(
                "thread-1",
                expect.objectContaining({
                    unreadUser: 1,
                    unreadStaff: 0,
                    status: "en_proceso",
                    lastSenderRole: "staff",
                    lastPreview: "Respuesta del panel"
                })
            );
        });

        it("respeta nextStatus si se pasa", async () => {
            repo.postMessage.mockResolvedValue(makeMessage({ senderRole: "staff" }));
            repo.getThread.mockResolvedValue(makeThread({ unreadUser: 2 }));

            await useCase.execute({
                threadId: "thread-1",
                senderRole: "staff",
                senderId: "staff-1",
                senderName: "Soporte",
                body: "cerramos",
                nextStatus: "resuelto"
            });

            expect(repo.touchThread).toHaveBeenCalledWith(
                "thread-1",
                expect.objectContaining({
                    unreadUser: 3,
                    unreadStaff: 0,
                    status: "resuelto"
                })
            );
        });

        it("clampa unreadUser a 99", async () => {
            repo.postMessage.mockResolvedValue(makeMessage({ senderRole: "staff" }));
            repo.getThread.mockResolvedValue(makeThread({ unreadUser: 99 }));

            await useCase.execute({
                threadId: "thread-1",
                senderRole: "staff",
                senderId: "staff-1",
                senderName: "Soporte",
                body: "más"
            });

            expect(repo.touchThread).toHaveBeenCalledWith(
                "thread-1",
                expect.objectContaining({ unreadUser: 99, unreadStaff: 0 })
            );
        });

        it("fallback unreadUser=1 si getThread falla", async () => {
            repo.postMessage.mockResolvedValue(makeMessage({ senderRole: "staff" }));
            repo.getThread.mockRejectedValue(new Error("timeout"));

            await useCase.execute({
                threadId: "thread-1",
                senderRole: "staff",
                senderId: "staff-1",
                senderName: "Soporte",
                body: "hola"
            });

            expect(repo.touchThread).toHaveBeenCalledWith(
                "thread-1",
                expect.objectContaining({
                    unreadUser: 1,
                    unreadStaff: 0,
                    status: "en_proceso"
                })
            );
        });
    });

    describe("resiliencia", () => {
        it("soft-fail si touchThread falla: no lanza y devuelve el mensaje", async () => {
            const msg = makeMessage();
            repo.postMessage.mockResolvedValue(msg);
            repo.getThread.mockResolvedValue(makeThread({ unreadStaff: 1 }));
            repo.touchThread.mockRejectedValue(new Error("permission denied"));

            const result = await useCase.execute({
                threadId: "thread-1",
                senderRole: "user",
                senderId: "user-1",
                senderName: "Usuario",
                body: "mensaje persistido"
            });

            expect(result).toEqual(msg);
            expect(repo.postMessage).toHaveBeenCalled();
            expect(repo.touchThread).toHaveBeenCalled();
        });

        it("propaga error si postMessage falla (no llega a touch)", async () => {
            repo.postMessage.mockRejectedValue(new Error("write failed"));

            await expect(
                useCase.execute({
                    threadId: "thread-1",
                    senderRole: "user",
                    senderId: "user-1",
                    senderName: "Usuario",
                    body: "hola"
                })
            ).rejects.toThrow("write failed");

            expect(repo.touchThread).not.toHaveBeenCalled();
        });

        it("trunca lastPreview a ~180 chars", async () => {
            const long = "x".repeat(200);
            repo.postMessage.mockResolvedValue(makeMessage({ body: long }));
            repo.getThread.mockResolvedValue(makeThread());

            await useCase.execute({
                threadId: "thread-1",
                senderRole: "user",
                senderId: "user-1",
                senderName: "Usuario",
                body: long
            });

            const patch = repo.touchThread.mock.calls[0][1];
            expect(patch.lastPreview.length).toBeLessThanOrEqual(180);
            expect(patch.lastPreview.endsWith("…")).toBe(true);
        });
    });
});
