import type {
  AgentConnectionResult,
  AgentMessage,
  AgentReply,
} from "../../domain/entity/AgentMessage";
import type { AgentRepository } from "../../domain/repository/agent.repository";

export interface MistralAgentConfig {
  apiKey: string;
  agentId: string;
  /** Used for connection probe — e.g. mistral-medium-latest */
  modelId: string;
}

const MISTRAL_API_BASE = "https://api.mistral.ai/v1";

function newId(): string {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }
  return `msg_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`;
}

function extractTextContent(content: unknown): string {
  if (typeof content === "string") return content;
  if (Array.isArray(content)) {
    return content
      .map((part) => {
        if (typeof part === "string") return part;
        if (part && typeof part === "object" && "text" in part) {
          return String((part as { text: unknown }).text ?? "");
        }
        return "";
      })
      .filter(Boolean)
      .join("\n");
  }
  if (content == null) return "";
  return String(content);
}

/**
 * Cliente Mistral por HTTP (fetch) — **sin** paquete `@mistralai/mistralai`
 * (versiones 2.2.2–2.2.4 fueron parte de la campaña Mini Shai-Hulud; evitamos el SDK npm).
 *
 * - Probe: GET /v1/models/{model_id}
 * - Chat:  POST /v1/agents/completions
 */
export class AgentMistralRepository implements AgentRepository {
  constructor(private readonly config: MistralAgentConfig) {}

  private headers(): HeadersInit {
    return {
      Authorization: `Bearer ${this.config.apiKey}`,
      Accept: "application/json",
      "Content-Type": "application/json",
    };
  }

  async checkConnection(): Promise<AgentConnectionResult> {
    if (!this.config.apiKey?.trim()) {
      return {
        status: "unconfigured",
        modelId: null,
        message: "Falta VITE_MISTRAL_API_KEY / MISTRAL_API_KEY",
      };
    }
    if (!this.config.agentId?.trim()) {
      return {
        status: "unconfigured",
        modelId: null,
        message: "Falta VITE_MISTRAL_AGENT_ID / MISTRAL_AGENT_ID",
      };
    }

    const modelId = this.config.modelId?.trim() || "mistral-medium-latest";

    try {
      const res = await fetch(`${MISTRAL_API_BASE}/models/${encodeURIComponent(modelId)}`, {
        method: "GET",
        headers: this.headers(),
      });
      if (!res.ok) {
        const body = await res.text().catch(() => "");
        return {
          status: "error",
          modelId,
          message: `Mistral models HTTP ${res.status}: ${body.slice(0, 180)}`,
        };
      }
      const data = (await res.json()) as { id?: string };
      return {
        status: "ok",
        modelId: data.id ?? modelId,
        message: `Conectado a Mistral (model=${data.id ?? modelId}, agent=${this.config.agentId})`,
      };
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      return {
        status: "error",
        modelId,
        message: `Error de conexión Mistral: ${msg.slice(0, 200)}`,
      };
    }
  }

  async sendMessage(
    userText: string,
    history?: ReadonlyArray<Pick<AgentMessage, "role" | "content">>
  ): Promise<AgentReply> {
    if (!this.config.apiKey?.trim() || !this.config.agentId?.trim()) {
      throw new Error(
        "Agente Mistral no configurado (VITE_MISTRAL_API_KEY / VITE_MISTRAL_AGENT_ID)"
      );
    }

    const messages: Array<{ role: string; content: string }> = [];
    if (history?.length) {
      for (const h of history) {
        if (h.role === "user" || h.role === "assistant" || h.role === "system") {
          messages.push({ role: h.role, content: h.content });
        }
      }
    }
    messages.push({ role: "user", content: userText });

    const res = await fetch(`${MISTRAL_API_BASE}/agents/completions`, {
      method: "POST",
      headers: this.headers(),
      body: JSON.stringify({
        agent_id: this.config.agentId,
        messages,
      }),
    });

    if (!res.ok) {
      const body = await res.text().catch(() => "");
      throw new Error(`Mistral agents HTTP ${res.status}: ${body.slice(0, 240)}`);
    }

    const data = (await res.json()) as {
      id?: string;
      choices?: Array<{ message?: { content?: unknown } }>;
    };
    const raw = data.choices?.[0]?.message?.content;
    const text = extractTextContent(raw).trim() || "(sin respuesta)";

    return {
      providerId: data.id ?? null,
      message: {
        id: newId(),
        role: "assistant",
        content: text,
        createdAt: new Date().toISOString(),
      },
    };
  }
}
