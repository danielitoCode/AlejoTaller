export type SupportReason = "soporte" | "pregunta_tecnica" | "facturacion" | "otro";
export type SupportStatus = "nuevo" | "en_proceso" | "resuelto";

export interface SupportMessage {
    id: string;
    createdAtIso: string;
    fromName: string;
    fromEmail: string;
    reason: SupportReason;
    status: SupportStatus;
    subject: string;
    body: string;
}

