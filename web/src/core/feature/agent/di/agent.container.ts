import { ENV } from "../../../infrastructure/env";
import { AgentMistralRepository } from "../data/repository/agent.mistral.repository";
import { CheckAgentConnectionCaseUse } from "../domain/caseuse/CheckAgentConnectionCaseUse";
import { SendAgentMessageCaseUse } from "../domain/caseuse/SendAgentMessageCaseUse";
import type { AgentRepository } from "../domain/repository/agent.repository";

function buildRepo(): AgentRepository {
  return new AgentMistralRepository({
    apiKey: (ENV.mistralApiKey as string | undefined)?.trim() ?? "",
    agentId: (ENV.mistralAgentId as string | undefined)?.trim() ?? "",
    modelId:
      (ENV.mistralModelId as string | undefined)?.trim() ||
      "mistral-medium-latest",
  });
}

const repo = buildRepo();
const checkConnection = new CheckAgentConnectionCaseUse(repo);
const sendMessage = new SendAgentMessageCaseUse(repo);

export const agentContainer = {
  repositories: { net: repo },
  useCases: {
    checkConnection: () => checkConnection.execute(),
    sendMessage: (text: string, history?: Parameters<SendAgentMessageCaseUse["execute"]>[0]["history"]) =>
      sendMessage.execute({ text, history }),
  },
};
