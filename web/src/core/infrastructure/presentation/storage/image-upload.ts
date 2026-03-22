import { ID } from "appwrite";
import { storage } from "../../di/appwrite.config";
import { ENV } from "../../env";

function toUrlString(value: unknown): string {
    if (typeof value === "string") return value;
    try {
        // eslint-disable-next-line @typescript-eslint/no-base-to-string
        return String(value);
    } catch {
        return "";
    }
}

export async function uploadImageToStorage(file: File): Promise<string> {
    const bucketId = ENV.storageBucketId;
    if (!bucketId) throw new Error("Falta configurar VITE_APPWRITE_STORAGE_BUCKET_ID");

    const created = await storage.createFile(bucketId, ID.unique(), file);
    const view = storage.getFileView(bucketId, created.$id);
    const url = toUrlString(view);
    if (!url) throw new Error("No se pudo obtener la URL del archivo subido.");
    return url;
}

