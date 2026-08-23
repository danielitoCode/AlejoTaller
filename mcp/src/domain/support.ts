/**
 * Domain — Support
 *
 * Maps to the `support_threads` and `support_messages` Appwrite collections.
 *
 * A SupportThread is the top-level conversation container.
 * SupportChatMessages are the individual messages within a thread.
 * Customers can only see their own threads and messages.
 */
export type SupportReason =
  | "soporte"
  | "pregunta_tecnica"
  | "facturacion"
  | "otro";

export type SupportStatus =
  | "nuevo"
  | "en_proceso"
  | "resuelto"
  | "cerrado";

export type SupportSenderRole = "user" | "staff";

export interface SupportThread {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  reason: SupportReason;
  subject: string;
  status: SupportStatus;
  lastMessageAt: string;
  lastPreview: string;
  lastSenderRole: SupportSenderRole;
  unreadUser: number;
  createdAt: string;
}

export interface SupportChatMessage {
  id: string;
  threadId: string;
  senderRole: SupportSenderRole;
  senderId: string;
  senderName: string;
  body: string;
  createdAt: string;
}

/** Input for creating a new support thread */
export interface CreateSupportThreadInput {
  reason: SupportReason;
  subject: string;
  /** First message body */
  body: string;
}

/** Input for posting a message in an existing thread */
export interface PostSupportMessageInput {
  threadId: string;
  body: string;
}

/** Human-readable label for support status */
export function supportStatusLabel(status: SupportStatus): string {
  switch (status) {
    case "nuevo":
      return "Nuevo";
    case "en_proceso":
      return "En proceso";
    case "resuelto":
      return "Resuelto";
    case "cerrado":
      return "Cerrado";
  }
}

/** Human-readable label for support reason */
export function supportReasonLabel(reason: SupportReason): string {
  switch (reason) {
    case "soporte":
      return "Soporte técnico";
    case "pregunta_tecnica":
      return "Pregunta técnica";
    case "facturacion":
      return "Facturación";
    case "otro":
      return "Otro";
  }
}
