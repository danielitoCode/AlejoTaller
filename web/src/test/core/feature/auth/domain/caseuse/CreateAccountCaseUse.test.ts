/**
 * CreateAccountCaseUse — thin delegate to userRepository.createAccount.
 * Validación de email/password vive en UI / Appwrite; no en el case use.
 */
import { describe, it, expect, beforeEach, vi } from "vitest";
import { CreateAccountCaseUse } from "../../../../../../core/feature/auth/domain/caseuse/CreateAccountCaseUse";
import type { UserNetRepository } from "../../../../../../core/feature/auth/domain/repository/user.net.repository";

describe("CreateAccountCaseUse", () => {
    let mockRepository: { createAccount: ReturnType<typeof vi.fn> };
    let useCase: CreateAccountCaseUse;

    function createValidUser() {
        return {
            email: "usuario@ejemplo.com",
            password: "ContraseñaSegura123!",
            name: "Usuario Test",
            phone: "+5355123456",
            photo_url: "",
            role: "customer"
        };
    }

    beforeEach(() => {
        mockRepository = {
            createAccount: vi.fn().mockResolvedValue(undefined)
        };
        useCase = new CreateAccountCaseUse(mockRepository as unknown as UserNetRepository);
    });

    it("delegates to repository.createAccount", async () => {
        const user = createValidUser();
        await useCase.execute(user);
        expect(mockRepository.createAccount).toHaveBeenCalledWith(user);
    });

    it("propagates repository errors", async () => {
        mockRepository.createAccount.mockRejectedValue(new Error("email already exists"));
        await expect(useCase.execute(createValidUser())).rejects.toThrow("email already exists");
    });
});
