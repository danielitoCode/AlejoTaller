/**
 * @file MarkThreadReadCaseUse.test.ts
 * @description Unit tests for mark-as-read (reset unread counters).
 */

import { describe, it, expect, beforeEach, vi } from "vitest";
import { MarkThreadReadCaseUse } from "../../../../../../core/feature/support/domain/caseuse/MarkThreadReadCaseUse";
import type { SupportRepository } from "../../../../../../core/feature/support/domain/repository/support.repository";

describe("MarkThreadReadCaseUse", () => {
    let repo: { touchThread: ReturnType<typeof vi.fn> };
    let useCase: MarkThreadReadCaseUse;

    beforeEach(() => {
        repo = {
            touchThread: vi.fn().mockResolvedValue(undefined)
        };
        useCase = new MarkThreadReadCaseUse(repo as unknown as SupportRepository);
        vi.clearAllMocks();
    });

    it("marca unreadUser=0 cuando forRole es user", async () => {
        await useCase.execute("thread-abc", "user");

        expect(repo.touchThread).toHaveBeenCalledWith("thread-abc", { unreadUser: 0 });
        expect(repo.touchThread).toHaveBeenCalledTimes(1);
    });

    it("marca unreadStaff=0 cuando forRole es staff", async () => {
        await useCase.execute("thread-xyz", "staff");

        expect(repo.touchThread).toHaveBeenCalledWith("thread-xyz", { unreadStaff: 0 });
    });

    it("trimea threadId", async () => {
        await useCase.execute("  thread-1  ", "user");

        expect(repo.touchThread).toHaveBeenCalledWith("thread-1", { unreadUser: 0 });
    });

    it("rechaza threadId vacío", async () => {
        await expect(useCase.execute("", "user")).rejects.toThrow(
            "threadId requerido para marcar como leído"
        );
        await expect(useCase.execute("   ", "staff")).rejects.toThrow(
            "threadId requerido para marcar como leído"
        );
        expect(repo.touchThread).not.toHaveBeenCalled();
    });

    it("propaga error de touchThread", async () => {
        repo.touchThread.mockRejectedValue(new Error("permission denied"));

        await expect(useCase.execute("thread-1", "user")).rejects.toThrow("permission denied");
    });
});
