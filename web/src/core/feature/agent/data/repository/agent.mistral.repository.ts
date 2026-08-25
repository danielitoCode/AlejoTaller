import { Mistral } from "@mistralai/mistralai";
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
 * Mistral Agents via official TypeScript SDK (`@mistralai/mistralai`).
 * Fase 1: connection probe + agent completion (no MCP tools yet).
 */
export class AgentMistralRepository implements AgentRepository {
  private readonly client: Mistral;

  constructor(private readonly config: MistralAgentConfig) {
    this.client = new Mistral({ apiKey: config.apiKey });
  }

  async checkConnection(): Promise<AgentConnectionResult> {
    if (!this.config.apiKey?.trim()) {
      return {
        status: "unconfigured",
        modelId: null,
        message: "Falta VITE_MISTRAL_API_KEY",
      };
    }
    if (!this.config.agentId?.trim()) {
      return {
        status: "unconfigured",
        modelId: null,
        message: "Falta VITE_MISTRAL_AGENT_ID",
      };
    }

    const modelId = this.config.modelId?.trim() || "mistral-medium-latest";

    try {
      // Probe: retrieve model metadata (GET /v1/models/{model_id})
      await this.client.models.retrieve({ modelId });
      return {
        status: "ok",
        modelId,
        message: `Conectado a Mistral (model=${modelId}, agent=${this.config.agentId})`,
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

    const response = await this.client.agents.complete({
      agentId: this.config.agentId,
      messages: messages as never,
    });

    const choice = response.choices?.[0];
    const rawContent = choice?.message?.content;
    const text = extractTextContent(rawContent).trim() || "(sin respuesta)";

    return {
      providerId: response.id ?? null,
      message: {
        id: newId(),
        role: "assistant",
        content: text,
        createdAt: new Date().toISOString(),
      },
    };
  }
}
