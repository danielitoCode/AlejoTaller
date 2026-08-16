/**
 * UpdateNameCaseUse — thin delegate to userRepository.updateName.
 */
import { describe, it, expect, beforeEach, vi } from "vitest";
import { UpdateNameCaseUse } from "../../../../../../core/feature/auth/domain/caseuse/UpdateNameCaseUse";
import type { UserNetRepository } from "../../../../../../core/feature/auth/domain/repository/user.net.repository";

describe("UpdateNameCaseUse", () => {
    let mockRepository: { updateName: ReturnType<typeof vi.fn> };
    let useCase: UpdateNameCaseUse;

    beforeEach(() => {
        mockRepository = {
            updateName: vi.fn().mockResolvedValue(undefined)
        };
        useCase = new UpdateNameCaseUse(mockRepository as unknown as UserNetRepository);
    });

    it("delegates to repository.updateName", async () => {
        await useCase.execute("Nuevo Nombre");
        expect(mockRepository.updateName).toHaveBeenCalledWith("Nuevo Nombre");
    });

    it("propagates repository errors", async () => {
        mockRepository.updateName.mockRejectedValue(new Error("network"));
        await expect(useCase.execute("X")).rejects.toThrow("network");
    });
});
