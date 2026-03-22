import { ENV } from "../../../../infrastructure/env";
import type { PasswordResetNetRepository } from "../../domain/repository/password-reset.net.repository";
import { infrastructureContainer } from "../../../../infrastructure/di/infrastructure.container";

type PasswordResetHttpResponse = { success: true } | { success: false; error?: string };

export class PasswordResetNetRepositoryImpl implements PasswordResetNetRepository {
    private get baseUrl(): string {
        const url = (ENV.passwordResetUrl || "").trim();
        if (!url) throw new Error("Falta configurar VITE_PASSWORD_RESET_URL");
        return url.replace(/\/$/, "");
    }

    private async getJwt(): Promise<string> {
        const account = infrastructureContainer.appwrite.account;
        const jwt = await account.createJWT();
        return jwt.jwt;
    }

    private async post(path: string, body?: any): Promise<void> {
        const jwt = await this.getJwt();
        const res = await fetch(`${this.baseUrl}${path}`, {
            method: "POST",
            headers: {
                "content-type": "application/json",
                "x-appwrite-user-jwt": jwt
            },
            body: JSON.stringify(body ?? {})
        });

        const text = await res.text();
        let data: PasswordResetHttpResponse;
        try {
            data = JSON.parse(text || "{}");
        } catch {
            throw new Error("El servidor devolvió una respuesta inválida.");
        }

        if (!res.ok || !data || (data as any).success !== true) {
            const message = (data as any)?.error || "Error en verificación de seguridad";
            throw new Error(message);
        }
    }

    async requestCode(): Promise<void> {
        await this.post("/password-reset/request");
    }

    async confirmCode(params: { code: string; newPassword: string }): Promise<void> {
        await this.post("/password-reset/confirm", params);
    }
}

