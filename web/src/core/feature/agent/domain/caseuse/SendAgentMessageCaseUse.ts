import type { AgentMessage, AgentReply } from "../entity/AgentMessage";
import type { AgentRepository } from "../repository/agent.repository";

export class SendAgentMessageCaseUse {
  constructor(private readonly repo: AgentRepository) {}

  async execute(input: {
    text: string;
    history?: ReadonlyArray<Pick<AgentMessage, "role" | "content">>;
  }): Promise<AgentReply> {
    const text = input.text?.trim() ?? "";
    if (!text) {
      throw new Error("El mensaje no puede estar vacío");
    }
    return this.repo.sendMessage(text, input.history);
  }
}
