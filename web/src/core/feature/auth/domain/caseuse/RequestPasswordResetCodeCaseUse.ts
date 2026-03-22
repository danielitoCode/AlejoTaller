import type { PasswordResetNetRepository } from "../repository/password-reset.net.repository";

export class RequestPasswordResetCodeCaseUse {
    constructor(private readonly repo: PasswordResetNetRepository) {}

    async execute(): Promise<void> {
        await this.repo.requestCode();
    }
}

