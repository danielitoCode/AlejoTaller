/**
 * Shared tool helpers — re-export to avoid circular imports between tool modules.
 */
export {
  okJson,
  failText,
  mapToolError,
  sanitizeErrorMessage,
  confirmationHint,
  type ToolResult,
  type ToolSuccess,
  type ToolFailure,
} from "./result.js";

export { runTool, runAuthedTool, type AuthResolver } from "./run-tool.js";
