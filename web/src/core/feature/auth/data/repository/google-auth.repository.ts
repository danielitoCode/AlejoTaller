import type { GoogleAuthExchangeResult, GoogleAuthNetRepository } from "../../domain/repository/google-auth.net.repository";
import { ENV } from "../../../../infrastructure/env";

type GoogleAuthHttpResponse =
    | ({ success: true } & GoogleAuthExchangeResult)
    | { success: false; error?: string; code?: string | number };

export class GoogleAuthNetRepositoryImpl implements GoogleAuthNetRepository {
    private get baseUrl(): string {
        const url = (ENV.googleAuthUrl || "").trim();
        if (!url) {
            throw new Error(
                "VITE_GOOGLE_AUTH_URL no está configurada. El flujo MVP actual usa Google -> credencial -> sub -> login/registro directo en Appwrite."
            );
        }
        return url.replace(/\/$/, "");
    }

    async exchangeCredential(params: { credential: string; allowCreate?: boolean }): Promise<GoogleAuthExchangeResult> {
        // Uso futuro:
        // este endpoint permite delegar la validación de Google a un backend/Worker
        // y devolver una sesión Appwrite o una decisión de vínculo/registro.
        // Se mantiene aquí para no romper el contrato del dominio cuando se reactive.
        const res = await fetch(this.baseUrl, {
            method: "POST",
            headers: { "content-type": "application/json" },
            body: JSON.stringify({
                action: "exchange",
                credential: params.credential,
                allowCreate: Boolean(params.allowCreate)
            })
        });

        const text = await res.text();
        let data: GoogleAuthHttpResponse;
        try {
            data = JSON.parse(text || "{}");
        } catch {
            throw new Error("El servidor devolvió una respuesta inválida.");
        }

        if (!res.ok || !data || (data as any).success !== true) {
            const message = (data as any)?.error || "Error al autenticar con Google";
            throw new Error(message);
        }

        const { success: _ignore, ...payload } = data as any;
        return payload as GoogleAuthExchangeResult;
    }
}
