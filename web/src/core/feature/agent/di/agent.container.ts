import { ENV } from "../../../infrastructure/env";
import { AgentMistralRepository } from "../data/repository/agent.mistral.repository";
import { McpHttpRepository } from "../data/repository/mcp.http.repository";
import { CallMcpToolCaseUse } from "../domain/caseuse/CallMcpToolCaseUse";
import { CheckAgentConnectionCaseUse } from "../domain/caseuse/CheckAgentConnectionCaseUse";
import { CheckMcpHealthCaseUse } from "../domain/caseuse/CheckMcpHealthCaseUse";
import { ListMcpToolsCaseUse } from "../domain/caseuse/ListMcpToolsCaseUse";
import {
  RunAgentTurnCaseUse,
  type RunAgentTurnInput,
} from "../domain/caseuse/RunAgentTurnCaseUse";
import { SendAgentMessageCaseUse } from "../domain/caseuse/SendAgentMessageCaseUse";
import type { AgentRepository } from "../domain/repository/agent.repository";
import type { McpGateway } from "../domain/repository/mcp.gateway";
import type { McpAuthContext } from "../domain/entity/McpTypes";

function buildAgentRepo(): AgentRepository {
  return new AgentMistralRepository({
    apiKey: (ENV.mistralApiKey as string | undefined)?.trim() ?? "",
    agentId: (ENV.mistralAgentId as string | undefined)?.trim() ?? "",
    modelId:
      (ENV.mistralModelId as string | undefined)?.trim() ||
      "mistral-medium-latest",
  });
}

function buildMcpGateway(): McpGateway {
  const baseUrl =
    (ENV.mcpBaseUrl as string | undefined)?.trim() ||
    "https://alejotaller-mcp.daniel-imbert96.workers.dev";
  return new McpHttpRepository({ baseUrl });
}

const agentRepo = buildAgentRepo();
const mcpGateway = buildMcpGateway();

const checkConnection = new CheckAgentConnectionCaseUse(agentRepo);
const sendMessage = new SendAgentMessageCaseUse(agentRepo);
const checkMcpHealth = new CheckMcpHealthCaseUse(mcpGateway);
const listMcpTools = new ListMcpToolsCaseUse(mcpGateway);
const callMcpTool = new CallMcpToolCaseUse(mcpGateway);
const runAgentTurn = new RunAgentTurnCaseUse(agentRepo, callMcpTool);

export const agentContainer = {
  repositories: {
    net: agentRepo,
    mcp: mcpGateway,
  },
  useCases: {
    checkConnection: () => checkConnection.execute(),
    sendMessage: (
      text: string,
      history?: Parameters<SendAgentMessageCaseUse["execute"]>[0]["history"]
    ) => sendMessage.execute({ text, history }),
    runAgentTurn: (input: RunAgentTurnInput) => runAgentTurn.execute(input),
    checkMcpHealth: () => checkMcpHealth.execute(),
    listMcpTools: (auth?: McpAuthContext | null) => listMcpTools.execute(auth),
    callMcpTool: (input: {
      name: string;
      args?: Record<string, unknown>;
      auth?: McpAuthContext | null;
      isGuest?: boolean;
      userConfirmed?: boolean;
    }) => callMcpTool.execute(input),
  },
};
