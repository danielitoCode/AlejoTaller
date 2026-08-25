import { describe, it, expect } from "vitest";
import { ZodError } from "zod";
import {
  mapToolError,
  sanitizeErrorMessage,
  okJson,
} from "../../src/mcp/tools/result.js";
import {
  AuthenticationError,
  AuthorizationError,
} from "../../src/auth/context.js";

describe("sanitizeErrorMessage", () => {
  it("takes first line only", () => {
    expect(sanitizeErrorMessage("boom\n  at foo.ts:1")).toBe("boom");
  });

  it("strips AppwriteException prefix", () => {
    expect(sanitizeErrorMessage("AppwriteException: Document not found")).toBe(
      "Document not found"
    );
  });
});

describe("mapToolError", () => {
  it("maps AuthenticationError", () => {
    const r = mapToolError(new AuthenticationError(), "Crear pedido");
    expect(r.isError).toBe(true);
    expect(r.content[0]!.text).toMatch(/autenticación requerida/i);
  });

  it("maps AuthorizationError", () => {
    const r = mapToolError(new AuthorizationError(), "Cancelar");
    expect(r.content[0]!.text).toMatch(/no tienes permiso/i);
  });

  it("maps ZodError with paths", () => {
    const err = new ZodError([
      {
        code: "too_small",
        minimum: 1,
        type: "string",
        inclusive: true,
        exact: false,
        message: "Required",
        path: ["orderId"],
      },
    ]);
    const r = mapToolError(err, "Consultar");
    expect(r.content[0]!.text).toMatch(/parámetros inválidos/i);
    expect(r.content[0]!.text).toMatch(/orderId/);
  });

  it("preserves domain stock message", () => {
    const r = mapToolError(
      new Error('No hay disponibilidad para "Filtro" (pedido=5, disponible=1)'),
      "Crear pedido"
    );
    expect(r.content[0]!.text).toMatch(/disponibilidad/);
  });
});

describe("okJson", () => {
  it("serializes payload", () => {
    const r = okJson({ a: 1 });
    expect(r.content[0]!.text).toContain('"a": 1');
  });
});
