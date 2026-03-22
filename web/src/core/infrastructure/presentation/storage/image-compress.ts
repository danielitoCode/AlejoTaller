export type ImageCompressOptions = {
    maxSide?: number;
    quality?: number;
    mimeType?: "image/webp" | "image/jpeg";
};

export async function compressImageFile(
    file: File,
    options: ImageCompressOptions = {}
): Promise<File> {
    const { maxSide = 1600, quality = 0.86, mimeType = "image/webp" } = options;

    if (!file.type.startsWith("image/")) return file;

    try {
        const bitmap = await createImageBitmap(file);

        const longestSide = Math.max(bitmap.width, bitmap.height);
        const scale = longestSide > maxSide ? maxSide / longestSide : 1;

        const targetWidth = Math.max(1, Math.round(bitmap.width * scale));
        const targetHeight = Math.max(1, Math.round(bitmap.height * scale));

        const canvas = document.createElement("canvas");
        canvas.width = targetWidth;
        canvas.height = targetHeight;

        const ctx = canvas.getContext("2d");
        if (!ctx) return file;

        ctx.drawImage(bitmap, 0, 0, targetWidth, targetHeight);
        bitmap.close?.();

        const blob: Blob | null = await new Promise((resolve) => {
            canvas.toBlob((b) => resolve(b), mimeType, quality);
        });

        if (!blob) return file;

        const originalBase = file.name.replace(/\.[^/.]+$/, "");
        const nextExt = mimeType === "image/jpeg" ? "jpg" : "webp";
        const nextName = `${originalBase}.${nextExt}`;
        const out = new File([blob], nextName, { type: blob.type });

        return out.size < file.size ? out : file;
    } catch {
        return file;
    }
}

