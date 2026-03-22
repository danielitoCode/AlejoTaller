export interface SupportMessageDTO {
    // Compatible with Appwrite-like payloads (legacy) and custom APIs.
    id?: string;
    $id?: string;
    createdAtIso?: string;
    $createdAt?: string;
    created_at?: string;

    from_name?: string;
    from_email?: string;
    reason?: string;
    status?: string;
    subject?: string;
    body?: string;
}
