import type { SupportMessageDTO } from "../dto/SupportMessageDTO";
import type { SupportMessage, SupportReason, SupportStatus } from "../../domain/entity/SupportMessage";

function asReason(value: unknown): SupportReason {
    const v = String(value ?? "").toLowerCase();
    if (v === "soporte") return "soporte";
    if (v === "pregunta_tecnica") return "pregunta_tecnica";
    if (v === "facturacion") return "facturacion";
    return "otro";
}

function asStatus(value: unknown): SupportStatus {
    const v = String(value ?? "").toLowerCase();
    if (v === "en_proceso") return "en_proceso";
    if (v === "resuelto") return "resuelto";
    return "nuevo";
}

export function supportMessageFromDTO(dto: SupportMessageDTO): SupportMessage {
    return {
        id: dto.id ?? dto.$id ?? "",
        createdAtIso: dto.createdAtIso ?? dto.$createdAt ?? dto.created_at ?? new Date(0).toISOString(),
        fromName: dto.from_name ?? "",
        fromEmail: dto.from_email ?? "",
        reason: asReason(dto.reason),
        status: asStatus(dto.status),
        subject: dto.subject ?? "",
        body: dto.body ?? ""
    };
}

export type SupportMessageWriteDTO = Pick<SupportMessageDTO, "from_name" | "from_email" | "reason" | "status" | "subject" | "body">;

export function supportMessageToDTO(message: Omit<SupportMessage, "id" | "createdAtIso">): SupportMessageWriteDTO {
    return {
        from_name: message.fromName,
        from_email: message.fromEmail,
        reason: message.reason,
        status: message.status,
        subject: message.subject,
        body: message.body
    };
}
