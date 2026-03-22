import type { GoogleAuthExchangeResult, GoogleAuthNetRepository } from "../repository/google-auth.net.repository";

export class ExchangeGoogleCredentialCaseUse {
    constructor(private readonly googleAuthRepo: GoogleAuthNetRepository) {}

    async execute(params: { credential: string; allowCreate?: boolean }): Promise<GoogleAuthExchangeResult> {
        // Caso de uso reservado para una futura integración server-assisted.
        // El MVP actual autentica Google directamente con email + sub en Appwrite.
        return await this.googleAuthRepo.exchangeCredential(params);
    }
}
