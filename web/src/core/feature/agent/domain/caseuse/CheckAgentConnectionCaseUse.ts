import type { AgentConnectionResult } from "../entity/AgentMessage";
import type { AgentRepository } from "../repository/agent.repository";

export class CheckAgentConnectionCaseUse {
  constructor(private readonly repo: AgentRepository) {}

  execute(): Promise<AgentConnectionResult> {
    return this.repo.checkConnection();
  }
}
