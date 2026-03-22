import type { PasswordResetNetRepository } from "../repository/password-reset.net.repository";

export class ConfirmPasswordResetCodeCaseUse {
    constructor(private readonly repo: PasswordResetNetRepository) {}

    async execute(params: { code: string; newPassword: string }): Promise<void> {
        await this.repo.confirmCode(params);
    }
}

