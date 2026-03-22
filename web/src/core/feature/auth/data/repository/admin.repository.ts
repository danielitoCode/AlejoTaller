import type { Functions } from "appwrite";
import type { AdminNetRepository } from "../../domain/repository/admin.net.repository";
import type { User } from "../../domain/entity/User";
import { ENV } from "../../../../infrastructure/env";

type UsersFunctionResult = {
    success?: boolean;
    users?: any[];
    total?: number;
    error?: string;
    code?: string | number;
};

export class AdminNetManagerImpl implements AdminNetRepository {
    constructor(private readonly functionsClient: Functions) {}

    private get functionId(): string {
        const functionId = ENV.adminFunctionUsers || "";
        if (!functionId) throw new Error("Falta configurar VITE_APPWRITE_USERS_FUNCTION");
        return functionId;
    }

    private async exec<T>(payload: Record<string, unknown>): Promise<T> {
        const execution = await this.functionsClient.createExecution(this.functionId, JSON.stringify(payload), false);
        const status = execution.responseStatusCode ?? execution.status ?? "unknown";

        let result: any = {};
        try {
            result = JSON.parse(execution.responseBody || "{}");
        } catch {
            const preview = (execution.responseBody || "").slice(0, 300);
            throw new Error(`La Function devolvió una respuesta inválida (no JSON). status=${status}. body=${preview}`);
        }

        if (!result?.success) {
            const backendMessage = result?.error || "Error al ejecutar la Function";
            const backendCode = result?.code ? ` code=${result.code}` : "";
            throw new Error(`${backendMessage} (status=${status}${backendCode})`);
        }

        return result as T;
    }

    private mapAppwriteUser(raw: any): User {
        const labels: string[] = Array.isArray(raw?.labels)
            ? raw.labels.filter((v: any) => typeof v === "string")
            : [];
        const prefs = raw?.prefs && typeof raw.prefs === "object" ? raw.prefs : {};

        const roleFromLabels = labels.includes("owner")
            ? "owner"
            : labels.includes("admin")
              ? "admin"
              : labels.includes("sales")
                ? "sales"
                : labels.includes("viewer")
                  ? "viewer"
                  : null;

        const role = roleFromLabels ?? (typeof prefs?.role === "string" ? prefs.role : null);

        return {
            id: String(raw?.$id ?? raw?.id ?? ""),
            name: String(raw?.name ?? ""),
            email: String(raw?.email ?? ""),
            phone: String(raw?.phone ?? ""),
            password: "",
            photo_url: typeof prefs?.photo_url === "string" ? prefs.photo_url : "",
            sub: typeof prefs?.sub === "string" ? prefs.sub : "",
            verification: Boolean(raw?.emailVerification ?? raw?.email_verification ?? false),
            role,
            status: typeof raw?.status === "boolean" ? raw.status : undefined,
            labels
        };
    }

    async listUsers(params?: { search?: string }): Promise<{ total: number; users: User[] }> {
        const search = params?.search?.trim();
        const result = await this.exec<UsersFunctionResult>({
            action: "list",
            search: search && search.length ? search : undefined
        });

        const users = (result.users ?? []).map((u) => this.mapAppwriteUser(u));
        const total = typeof result.total === "number" ? result.total : users.length;
        return { total, users };
    }

    async createUser(params: { name: string; email?: string; phone?: string; password: string; labels?: string[] }): Promise<User> {
        const result = await this.exec<{ success: boolean; user: any }>({
            action: "create",
            name: params.name,
            email: params.email,
            phone: params.phone,
            password: params.password,
            labels: params.labels
        });

        return this.mapAppwriteUser(result.user);
    }

    async updateUser(params: { userId: string; patch: Record<string, unknown> }): Promise<User> {
        const result = await this.exec<{ success: boolean; user: any }>({
            action: "update",
            userId: params.userId,
            ...params.patch
        });

        return this.mapAppwriteUser(result.user);
    }

    async deleteUser(params: { userId: string }): Promise<void> {
        await this.exec<{ success: boolean }>({
            action: "delete",
            userId: params.userId
        });
    }
}
