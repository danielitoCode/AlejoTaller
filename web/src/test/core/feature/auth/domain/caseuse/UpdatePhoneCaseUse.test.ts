/**
 * UpdatePhoneCaseUse — thin delegate to userRepository.updatePhone.
 */
import { describe, it, expect, beforeEach, vi } from "vitest";
import { UpdatePhoneCaseUse } from "../../../../../../core/feature/auth/domain/caseuse/UpdatePhoneCaseUse";
import type { UserNetRepository } from "../../../../../../core/feature/auth/domain/repository/user.net.repository";

describe("UpdatePhoneCaseUse", () => {
    let mockRepository: { updatePhone: ReturnType<typeof vi.fn> };
    let useCase: UpdatePhoneCaseUse;

    beforeEach(() => {
        mockRepository = {
            updatePhone: vi.fn().mockResolvedValue(undefined)
        };
        useCase = new UpdatePhoneCaseUse(mockRepository as unknown as UserNetRepository);
    });

    it("delegates to repository.updatePhone", async () => {
        await useCase.execute("+5355123456");
        expect(mockRepository.updatePhone).toHaveBeenCalledWith("+5355123456");
    });

    it("propagates repository errors", async () => {
        mockRepository.updatePhone.mockRejectedValue(new Error("invalid phone"));
        await expect(useCase.execute("bad")).rejects.toThrow("invalid phone");
    });
});
